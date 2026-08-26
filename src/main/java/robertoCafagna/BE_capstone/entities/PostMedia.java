package robertoCafagna.BE_capstone.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.MediaType;

import java.util.UUID;

@Entity
@Getter
@ToString
@NoArgsConstructor
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Post post;

    @Column(nullable = false, length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Column
    private int orderIndex;

    @Column
    @Setter
    private String mediaPublicId;


    public PostMedia(Post post, String mediaUrl, MediaType type, int orderIndex) {
        this.post = post;
        this.mediaUrl = mediaUrl;
        this.type = type;
        this.orderIndex = orderIndex;
    }
}
