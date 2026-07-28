package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.InviteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "invited_user_id"})})
public class EventInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @ManyToOne
    @JoinColumn(name = "invited_user_id", nullable = false)
    @ToString.Exclude
    private User invitedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private InviteStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EventInvite(Event event, User invitedUser) {
        this.event = event;
        this.invitedUser = invitedUser;
        this.status = InviteStatus.PENDING;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
