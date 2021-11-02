
package io.helidon.example.lra.booking;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import io.helidon.microprofile.server.Server;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.glassfish.jersey.media.sse.OutboundEvent;

@Path("/booking")
@ApplicationScoped
public class BookingResource {

    private static final Logger LOG = Logger.getLogger(BookingResource.class.getSimpleName());

    private SseBroadcaster sseBroadcaster;

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    public static void main(String[] args) {
        Server.create().start();
    }

    @Inject
    BookingService repository;

    @PUT
    @Path("/create/{id}")
    // Create new LRA transaction which won't end after this JAX-RS method end
    // Time limit for new LRA is 30 sec
    @LRA(value = LRA.Type.REQUIRES_NEW, end = false, timeLimit = 30)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBooking(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                  @PathParam("id") long id,
                                  Booking booking) {

        // LRA ID assigned by coordinator is provided as artificial request header
        booking.setLraId(lraId.toASCIIString());

        if (repository.createBooking(booking, id)) {
            LOG.info("Creating booking for " + id);
            return Response.ok().build();
        } else {
            LOG.info("Seat " + id + " already booked!");
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(JSON.createObjectBuilder()
                            .add("error", "Seat " + id + " is already reserved!")
                            .add("seat", id)
                            .build())
                    .build();
        }
    }

    @PUT
    @Path("/payment")
    // Needs to be called within LRA transaction context
    // Doesn't end LRA transaction
    @LRA(value = LRA.Type.MANDATORY, end = false)
    @Produces(MediaType.APPLICATION_JSON)
    public Response makePayment(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                JsonObject jsonObject) {
        LOG.info("Payment " + jsonObject.toString());
        // Notice that we don't need to propagate LRA header
        // When using JAX-RS client, LRA header is propagated automatically
        ClientBuilder.newClient()
                .target("http://payment-service:7002")
                .path("/payment/confirm")
                .request()
                .rx()
                .put(Entity.entity(jsonObject, MediaType.APPLICATION_JSON))
                .whenComplete((res, t) -> {
                    if (res != null) {
                        LOG.info(res.getStatus() + " " + res.getStatusInfo().getReasonPhrase());
                        res.close();
                    }
                });
        return Response.accepted().build();
    }

    @Compensate
    public Response paymentFailed(URI lraId) {
        LOG.info("Payment failed! " + lraId);
        repository.clearBooking(lraId)
                .ifPresent(booking -> {
                    LOG.info("Booking for seat " + booking.getSeat().getId() + "cleared!");
                    Optional.ofNullable(sseBroadcaster)
                            .ifPresent(b -> b.broadcast(new OutboundEvent.Builder()
                                    .data(booking.getSeat())
                                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                    .build())
                            );
                });
        return Response.ok(ParticipantStatus.Completed.name()).build();
    }

    @Complete
    public Response paymentSuccessful(URI lraId) {
        LOG.info("Payment success! " + lraId);
        return Response.ok(ParticipantStatus.Completed.name()).build();
    }

    @GET
    @Path("/seat/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeat(@PathParam("id") long id) {
        try {
            Seat seat = repository.getSeatById(id);
            return Response.ok(seat).build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Path("/seat")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Seat> getAllBookedSeats() {
        LOG.info("Getting all booked seats.");
        return repository.getAllBookedSeats();
    }

    @GET
    @Path("sse-notifications")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToEvents(@Context SseEventSink eventSink, @Context Sse sse) {
        if (this.sseBroadcaster == null) {
            this.sseBroadcaster = sse.newBroadcaster();
        }
        sseBroadcaster.register(eventSink);
    }

}
