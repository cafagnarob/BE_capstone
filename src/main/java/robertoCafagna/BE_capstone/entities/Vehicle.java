package robertoCafagna.BE_capstone.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;


    @Column
    private String nickname;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private MotorcycleModel model;

    @Column
    private int year;

    @Column(unique = true)
    private String licensePlate;

    @Column
    private String vin;

    @Column
    private String color;

    @Column
    private int initialMileage;

    @Column
    @Setter
    private int currentMileage;

    @Column
    private String photoUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vehicle(User user, MotorcycleModel model, String nickname,
                   int year, String licensePlate, String vin, String color,
                   int initialMileage, String photoUrl
    ) {
        this.user = user;
        this.model = model;
        this.nickname = nickname;
        this.year = year;
        this.licensePlate = licensePlate;
        this.vin = vin;
        this.color = color;
        this.initialMileage = initialMileage;
        this.currentMileage = initialMileage;
        this.photoUrl = photoUrl;
    }


    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }


}
