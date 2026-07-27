package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class RidePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    @ToString.Exclude
    private Ride ride;


    @Column(nullable = false)
    private Double latitude;


    @Column(nullable = false)
    private Double longitude;


    @Column(nullable = false)
    private int sequence;


    private Double speedKmh;


    private Double altitude;


    @Column(nullable = false)
    private LocalDateTime recordedAt;


    public RidePoint(
            Ride ride,
            Double latitude,
            Double longitude,
            int sequence,
            Double speedKmh,
            Double altitude,
            LocalDateTime recordedAt
    ) {
        this.ride = ride;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequence = sequence;
        this.speedKmh = speedKmh;
        this.altitude = altitude;
        this.recordedAt = recordedAt;
    }
}