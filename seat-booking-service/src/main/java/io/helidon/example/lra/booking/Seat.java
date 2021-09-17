package io.helidon.example.lra.booking;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity(name = "Seat")
@Access(AccessType.PROPERTY)
@NamedQueries({
        @NamedQuery(name = "getAllSeats", query = "SELECT s FROM Seat s"),
        @NamedQuery(name = "getAllBookedSeats", query = "SELECT s FROM Booking b INNER JOIN b.seat s"),
        @NamedQuery(name = "getSeatById", query = "SELECT s FROM Seat s WHERE s.id = :id")
})
public class Seat {

    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    public Long getId() {
        return id;
    }
}
