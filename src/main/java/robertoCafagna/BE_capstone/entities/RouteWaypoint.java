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
    @Setter
    private double latitude;

    @Column(nullable = false)
    @Setter
    private double longitude;

    @Column(nullable = false)
    @Setter
    private int sequence;

    @Column
    @Setter
    private String label;

    @Column
    @Setter
    private String imageUrl;

    @Column
    @Setter
    private String imagePublicId;

    @Column
    @Setter
    private Integer stopMinutes;

    public RouteWaypoint(double latitude, double longitude, int sequence, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequence = sequence;
        this.label = label;
        this.stopMinutes = null;
    }


    public RouteWaypoint(double latitude, double longitude, int sequence, String label, Integer stopMinutes) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequence = sequence;
        this.label = label;
        this.stopMinutes = stopMinutes;
    }
}
