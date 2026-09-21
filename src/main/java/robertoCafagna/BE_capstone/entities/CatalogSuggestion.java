package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.CatalogSuggestionStatus;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class CatalogSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    private User reporter;

    @Column(nullable = false)
    private String brandName;

    @Column
    private String modelName;

    @Column
    private Integer engineCc;

    @Column
    @Enumerated(EnumType.STRING)
    private MotorcycleCategory category;

    @Column
    private Integer yearStart;

    @Column
    private Integer yearEnd;

    @Column
    private Integer horsePower;

    @Column
    private Integer weightKg;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private CatalogSuggestionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CatalogSuggestion(User reporter, String brandName, String modelName, Integer engineCc,
                             MotorcycleCategory category, Integer yearStart, Integer yearEnd,
                             Integer horsePower, Integer weightKg, String note) {
        this.reporter = reporter;
        this.brandName = brandName;
        this.modelName = modelName;
        this.engineCc = engineCc;
        this.category = category;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        this.horsePower = horsePower;
        this.weightKg = weightKg;
        this.note = note;
        this.status = CatalogSuggestionStatus.PENDING;
    }

    @PrePersist
    private void beforeInsert() {
        this.createdAt = LocalDateTime.now();
    }
}