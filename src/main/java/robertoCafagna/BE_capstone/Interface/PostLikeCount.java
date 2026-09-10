package robertoCafagna.BE_capstone.Interface;

import java.util.UUID;

public interface PostLikeCount {
    UUID getPostId();

    long getCount();
}
