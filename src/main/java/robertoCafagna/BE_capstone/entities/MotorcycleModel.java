package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class MotorcycleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @Setter
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;


    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private int engineCc;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MotorcycleCategory category;

    @Setter
    private int yearStart;

    @Setter
    private Integer yearEnd;

    @Setter
    @Column(nullable = false)
    private Integer horsePower;

    @Setter
    private Integer weightKg;

    @Setter
    private String imageUrl;

    @Setter
    private String imagePublicId;


    public MotorcycleModel(
            Brand brand,
            String name,
            int engineCc,
            MotorcycleCategory category,
            int yearStart,
            Integer yearEnd,
            Integer horsePower,
            Integer weightKg,
            String imageUrl
    ) {
        this.brand = brand;
        this.name = name;
        this.engineCc = engineCc;
        this.category = category;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        this.horsePower = horsePower;
        this.weightKg = weightKg;
        this.imageUrl = imageUrl;
    }
}
