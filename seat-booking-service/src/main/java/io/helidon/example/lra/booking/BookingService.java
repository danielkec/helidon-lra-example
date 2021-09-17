package io.helidon.example.lra.booking;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@ApplicationScoped
public class BookingService {

    @PersistenceContext(unitName = "booking")
    protected EntityManager entityManager;

    Seat getSeatById(long id) {
        return entityManager.createNamedQuery("getSeatById", Seat.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Seat> getAllSeats() {
        return entityManager.createNamedQuery("getAllSeats", Seat.class)
                .getResultList();
    }

    public List<Seat> getAllBookedSeats() {
        return entityManager.createNamedQuery("getAllBookedSeats", Seat.class)
                .getResultList();
    }

    public Optional<Booking> getBookingBySeat(long id) {
        return entityManager.createNamedQuery("getBookingBySeat", Booking.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Transactional
    public boolean createBooking(Booking booking, long id) {
        if (getBookingBySeat(id).isPresent()) {
            return false;
        }

        Seat seat = entityManager.createNamedQuery("getSeatById", Seat.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    Seat s = new Seat();
                    s.setId(id);
                    entityManager.persist(s);
                    return s;
                });

        booking.setSeat(seat);
        entityManager.persist(booking);
        entityManager.flush();
        return true;
    }
}
