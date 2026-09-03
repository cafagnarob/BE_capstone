package robertoCafagna.BE_capstone.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import robertoCafagna.BE_capstone.enums.WidgetSize;
import robertoCafagna.BE_capstone.enums.WidgetType;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class PostWidget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    private Post post;

    @ManyToOne
    @JoinColumn(name = "post_media_id")
    @ToString.Exclude
    private PostMedia media;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WidgetType type;

    @Column(nullable = false)
    private UUID referenceId; // rideId / routeId / eventId / motorcycleModelId, a seconda del tipo

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private WidgetSize size;

    @Column(nullable = false)
    private double xPercent; // 0-100, centro del widget

    @Column(nullable = false)
    private double yPercent; // 0-100, centro del widget

    public PostWidget(Post post, PostMedia media, WidgetType type, UUID referenceId, WidgetSize size, double xPercent, double yPercent) {
        this.post = post;
        this.media = media;
        this.type = type;
        this.referenceId = referenceId;
        this.size = size;
        this.xPercent = xPercent;
        this.yPercent = yPercent;
    }
}
