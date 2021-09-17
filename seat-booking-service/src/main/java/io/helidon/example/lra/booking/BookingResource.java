
package io.helidon.example.lra.booking;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.helidon.microprofile.server.Server;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

@Path("/booking")
@ApplicationScoped
public class BookingResource {

    Logger LOG = Logger.getLogger(BookingResource.class.getSimpleName());

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    public static void main(String[] args) {
        Server.create().start();
    }

    @Inject
    BookingService repository;

    @PUT
    @Path("/create/{id}")
    @LRA(LRA.Type.REQUIRES_NEW)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBooking(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                  @PathParam("id") long id,
                                  Booking booking) {
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

    @Compensate
    public Response paymentFailed(URI lraId) {
        LOG.info("Payment failed! " + lraId);
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

}
