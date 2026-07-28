package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    @Setter
    private User user;

    @Column(length = 500)
    @Setter
    private String description;

    @Column
    @Setter
    private String location;

    @Column
    @Setter
    private LocalDate birthDate;

    @OneToMany(
            mappedBy = "profile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<ProfileLink> links = new ArrayList<>();

    public UserProfile(String description, String location,
                       LocalDate birthDate) {
        this.description = description;
        this.location = location;
        this.birthDate = birthDate;
    }

    public void addLink(ProfileLink link) {
        links.add(link);
        link.setProfile(this);
    }

    public void removeLink(ProfileLink link) {
        links.remove(link);
        link.setProfile(null);
    }
}
