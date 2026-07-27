package robertoCafagna.BE_capstone.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import robertoCafagna.BE_capstone.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Setter
    private String username;

    @Column
    @Setter
    private String name;

    @Column
    @Setter
    private String surname;

    @Column(nullable = false, unique = true)
    @Setter
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    @Setter
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    @Setter
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    @Setter
    private boolean active;

    @Column(nullable = false)
    @Setter
    private String profilePicture;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL)
    @JsonIgnore
    private UserProfile profile;

    @JsonIgnore
    @OneToMany(mappedBy = "follower")
    @ToString.Exclude
    private List<FollowingRelationship> following = new ArrayList<>();


    @JsonIgnore
    @OneToMany(mappedBy = "followedUser")
    @ToString.Exclude
    private List<FollowingRelationship> followers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    @ToString.Exclude
    private List<Participation> participations = new ArrayList<>();

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "current_vehicle_id")
    private Vehicle currentVehicle;


    public User(String username, String email,
                String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        // TODO aggiungere foto di default
        this.profilePicture = "default/pic";
        // TODO aggiungere foto di default
        this.role = Role.USER;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
        active = false;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
