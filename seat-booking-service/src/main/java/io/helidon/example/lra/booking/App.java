package io.helidon.example.lra.booking;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Application;

@ApplicationScoped
@Deprecated//remove me !!!
public class App extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(BookingResource.class);
    }
}
