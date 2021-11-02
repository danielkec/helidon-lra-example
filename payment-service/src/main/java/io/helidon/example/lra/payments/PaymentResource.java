
package io.helidon.example.lra.payments;

import java.net.URI;
import java.util.Collections;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.helidon.microprofile.server.Server;

import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

@Path("/payment")
public class PaymentResource {

    private static final Logger LOG = Logger.getLogger(PaymentResource.class.getName());

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    public static void main(String[] args) {
        Server.create().start();
    }

    @PUT
    @Path("/confirm")
    // This resource method ends/commits LRA transaction as successfully completed
    @LRA(value = LRA.Type.MANDATORY, end = true)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makePayment(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) URI lraId,
                                Payment payment) {
        if (!payment.cardNumber.equals("0000-0000-0000")) {
            LOG.warning("Payment " + payment.cardNumber);
            throw new IllegalStateException("Card " + payment.cardNumber + " is not valid! "+lraId);
        }
        LOG.info("Payment " + payment.cardNumber+ " " +lraId);
        return Response.ok(JSON.createObjectBuilder().add("result", "success").build()).build();
    }

    @AfterLRA
    public void afterLra(URI lraId, LRAStatus lraStatus) {
        LOG.info("LRA finished " + lraId.toASCIIString() + " " + lraStatus.name());
    }
}
