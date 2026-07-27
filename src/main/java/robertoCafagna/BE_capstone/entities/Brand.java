package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, unique = true)
    @Setter
    private String name;

    @Column(length = 500)
    private String logoUrl;

    @OneToMany(
            mappedBy = "brand",
            cascade = CascadeType.ALL
    )
    @ToString.Exclude
    private List<MotorcycleModel> models = new ArrayList<>();


    public Brand(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }
}