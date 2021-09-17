
package io.helidon.example.lra.payments;

import java.util.Collections;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/greet")
public class GreetResource {
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private final String message;

    @Inject
    public GreetResource(@ConfigProperty(name = "app.greeting") String message) {
        this.message = message;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDefaultMessage() {
        String msg = String.format("%s %s!", message, "World");
        return JSON.createObjectBuilder()
                .add("message", msg)
                .add("app", "payment-service")
                .build();
    }
}
