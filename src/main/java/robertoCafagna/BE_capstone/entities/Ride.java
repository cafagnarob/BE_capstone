package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.RideType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@ToString
@Getter
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    @ToString.Exclude
    private Vehicle vehicle;

    @Column(length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Double distanceKm;

    @Column
    private Double avgSpeedKmH;

    @Column
    private Double maxSpeedKmH;

    @Column
    private int stopsCount;

    @Column
    private int totalStopDurationSeconds;

    @Column(length = 1000)
    private String notes;

    @OneToMany(
            mappedBy = "ride",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<RidePoint> points = new ArrayList<>();


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private RideType type;

    // inizio viaggio
    public Ride(
            User user,
            Vehicle vehicle,
            String title
    ) {
        this.user = user;
        this.vehicle = vehicle;
        this.title = title;

        this.startedAt = LocalDateTime.now();

        this.distanceKm = 0.0;
        this.avgSpeedKmH = 0.0;
        this.maxSpeedKmH = 0.0;
        this.stopsCount = 0;
        this.totalStopDurationSeconds = 0;
    }

    // fine viaggio
    public void finishRide(
            LocalDateTime endedAt,
            Double distanceKm,
            Double avgSpeed,
            Double maxSpeed,
            int stopsCount,
            int totalStopDurationSeconds
    ) {
        this.endedAt = endedAt;
        this.distanceKm = distanceKm;
        this.avgSpeedKmH = avgSpeed;
        this.maxSpeedKmH = maxSpeed;
        this.stopsCount = stopsCount;
        this.totalStopDurationSeconds = totalStopDurationSeconds;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
