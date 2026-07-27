package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@ToString
@Getter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    @ToString.Exclude
    private User organizer;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Double meetingPointLat;

    @Column(nullable = false)
    private Double meetingPointLng;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventVisibility visibility;

    @Column(length = 50)
    private String accessCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<Participation> participants = new ArrayList<>();


    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
