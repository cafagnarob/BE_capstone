package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.AccessRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "requester_id"})})
public class AccessCodeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    @ToString.Exclude
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private AccessRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AccessCodeRequest(Event event, User requester) {
        this.event = event;
        this.requester = requester;
        this.status = AccessRequestStatus.PENDING;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}