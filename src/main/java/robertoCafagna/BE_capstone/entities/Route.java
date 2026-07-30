package robertoCafagna.BE_capstone.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    @ToString.Exclude
    private User creator;

    @Column(nullable = false)
    @Setter
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String encodedPolyline; // geometria stradale calcolata da Mapbox

    @Column(nullable = false)
    private double distanceMeters;

    @Column(nullable = false)
    private double durationSeconds;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("sequence ASC")
    @ToString.Exclude
    private List<RouteWaypoint> waypoints = new ArrayList<>();

    @Column(nullable = false)
    private boolean avoidHighways;

    @Column(nullable = false)
    private boolean avoidTolls;

    @Column(nullable = false)
    private boolean avoidFerries;

    @Column(nullable = false)
    @Setter
    private boolean importable;

    public Route(User creator, String name, String encodedPolyline, double distanceMeters, double durationSeconds,
                 boolean avoidHighways, boolean avoidTolls, boolean avoidFerries) {
        this.creator = creator;
        this.name = name;
        this.encodedPolyline = encodedPolyline;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.avoidHighways = avoidHighways;
        this.avoidTolls = avoidTolls;
        this.avoidFerries = avoidFerries;
        this.importable = false;
    }

    public void addWaypoint(RouteWaypoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setRoute(this);
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
