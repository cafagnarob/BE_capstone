package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"event_id", "user_id"})})
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private ParticipationStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;


    public Participation(Event event, User user) {
        this.event = event;
        this.user = user;
        this.status = ParticipationStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
    }

    public Participation(Event event, User user, ParticipationStatus status) {
        this.event = event;
        this.user = user;
        this.status = status;
        this.joinedAt = LocalDateTime.now();
    }
}
