package robertoCafagna.BE_capstone.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.ReportReason;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.enums.ReportTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    private User reporter;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private ReportStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Report(User reporter, ReportTargetType targetType, UUID targetId, ReportReason reason, String note) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.note = note;
        this.status = ReportStatus.PENDING;
    }

    @PrePersist
    private void beforeInsert() {
        this.createdAt = LocalDateTime.now();
    }
}