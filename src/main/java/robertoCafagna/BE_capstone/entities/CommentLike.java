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
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    @ToString.Exclude
    private PostComment comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CommentLike(User user, PostComment comment) {
        this.user = user;
        this.comment = comment;
    }

    @PrePersist
    private void beforeInsert() {
        this.createdAt = LocalDateTime.now();
    }
}
