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
@ToString
@NoArgsConstructor
public class PostComment {
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

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public PostComment(User user, Post post, String text) {
        this.user = user;
        this.post = post;
        this.text = text;
    }

    @PrePersist
    private void beforeInsert() {
        createdAt = LocalDateTime.now();
    }

}
