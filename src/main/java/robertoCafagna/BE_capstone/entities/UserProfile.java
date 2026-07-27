package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class UserProfile {
    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    @Setter
    private String description;

    @Column
    @Setter
    private String location;

    @Column
    @Setter
    private LocalDate birthDate;

    @Column
    @Setter
    private String webSite;

    public UserProfile(String description, String location,
                       LocalDate birthDate, String webSite) {
        this.description = description;
        this.location = location;
        this.birthDate = birthDate;
        this.webSite = webSite;
    }
}
