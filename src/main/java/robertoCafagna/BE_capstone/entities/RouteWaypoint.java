package robertoCafagna.BE_capstone.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class RouteWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    @Setter
    private Route route;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private int sequence;

    @Column
    private String label;

    public RouteWaypoint(double latitude, double longitude, int sequence, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequence = sequence;
        this.label = label;
    }
}
