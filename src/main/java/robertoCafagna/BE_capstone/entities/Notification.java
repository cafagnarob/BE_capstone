package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.NotificationType;
import robertoCafagna.BE_capstone.enums.ReferenceType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    @ToString.Exclude
    private User actor;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    @Setter
    private boolean read;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private UUID referenceId;

    @Column
    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    public Notification(User user, User actor, NotificationType type, String message, UUID referenceId, ReferenceType referenceType) {
        this.user = user;
        this.actor = actor;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
    }

    @PrePersist
    private void beforeInsert() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
}
