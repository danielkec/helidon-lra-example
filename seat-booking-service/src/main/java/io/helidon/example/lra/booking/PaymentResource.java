package io.helidon.example.lra.booking;

import java.net.URI;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

@Path("/payment-proxy")
@ApplicationScoped
public class PaymentResource {

    private static final Logger LOG = Logger.getLogger(PaymentResource.class.getSimpleName());

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
                        LOG.info("Payment service response: "
                                + res.getStatus()
                                + " " + res.getStatusInfo().getReasonPhrase()
                                + " " +lraId);
                        res.close();
                    }
                });
        return Response.accepted().build();
    }

    @AfterLRA
    public void onLRAEnd(URI lraId, LRAStatus status) {
        LOG.info("Payment " + status + " " + lraId);
    }

}
