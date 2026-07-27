package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    private String title;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Double distanceKm;

    @Column
    private Double avgSpeed;

    @Column
    private Double maxSpeed;

    @Column
    private int stopsCount;

    @Column
    private int totalStopDurationSeconds;

    @Column
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

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
