package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.Platform;

import java.util.UUID;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class ProfileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    @Setter
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(length = 500)
    @Setter
    private String url;

    @Setter
    @ManyToOne
    @JoinColumn(name = "profile_id")
    @ToString.Exclude
    private UserProfile profile;

    public ProfileLink(Platform platform, String url) {
        this.platform = platform;
        this.url = url;
    }

}
