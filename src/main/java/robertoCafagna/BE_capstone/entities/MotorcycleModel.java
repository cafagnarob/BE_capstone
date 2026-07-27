package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int engineCc;

    @Enumerated(EnumType.STRING)
    private MotorcycleCategory category;

    private int yearStart;

    private Integer yearEnd;

    @Column(nullable = false)
    private int horsePower;

    private int weightKg;

    private String imageUrl;

}
