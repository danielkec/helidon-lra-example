package io.helidon.example.lra.booking;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

@Entity(name = "Booking")
@Access(AccessType.PROPERTY)
@NamedQueries({
        @NamedQuery(name = "getBookingBySeat",
                query = "SELECT b FROM Booking b INNER JOIN b.seat s WHERE s.id = :id"),
        @NamedQuery(name = "getBookingByLraId",
                query = "SELECT b FROM Booking b INNER JOIN b.seat s WHERE b.lraId = :lraId"),
})
public class Booking {

    private Long id;
    private String firstName;
    private String lraId;
    private Seat seat;

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLraId() {
        return lraId;
    }

    @OneToOne
    @JsonbTransient
    public Seat getSeat() {
        return seat;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
}
