package robertoCafagna.BE_capstone.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "post_likes",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"user_id", "post_id"})})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Post post;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }


    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }
}
