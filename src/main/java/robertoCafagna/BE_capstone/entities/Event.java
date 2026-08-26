package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventType;
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
    @Setter
    private String title;

    @Column(nullable = false)
    @Setter
    private String description;

    @Column(nullable = false)
    @Setter
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    @Setter
    private LocalDateTime endDateTime;

    @Column
    @Setter
    private Double meetingPointLat;

    @Column
    @Setter
    private Double meetingPointLng;

    @Column(nullable = false)
    @Setter
    private int maxParticipants;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private EventStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventVisibility visibility;

    @Column(length = 100)
    @Setter
    private String accessCode;

    @Column(nullable = false)
    @Setter
    private boolean autoApprove;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    @Setter
    private String meetingPointAddress;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<Participation> participants = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "route_id")
    @Setter
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private EventType type;

    @ManyToOne
    @JoinColumn(name = "parent_event_id")
    @Setter
    private Event parentEvent;

    @OneToMany(mappedBy = "parentEvent")
    @OrderBy("startDateTime ASC")
    private List<Event> children = new ArrayList<>();

    public Event(
            User organizer,
            String title,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Route route,
            Double meetingPointLat,
            Double meetingPointLng,
            int maxParticipants,
            EventVisibility visibility,
            String accessCode,
            boolean autoApprove,
            EventType type
    ) {
        this.organizer = organizer;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.route = route;
        this.meetingPointLat = meetingPointLat;
        this.meetingPointLng = meetingPointLng;
        this.maxParticipants = maxParticipants;
        this.visibility = visibility;
        this.accessCode = accessCode;
        this.autoApprove = autoApprove;
        this.status = EventStatus.ACTIVE;
        this.type = type;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
