package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
@Table(
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {
                        "follower_id",
                        "followed_user_id"})})
public class FollowingRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    @ToString.Exclude
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followed_user_id", nullable = false)
    @ToString.Exclude
    private User followedUser;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public FollowingRelationship(User follower, User followedUser) {
        this.follower = follower;
        this.followedUser = followedUser;
        this.createdAt = LocalDateTime.now();
    }
}
