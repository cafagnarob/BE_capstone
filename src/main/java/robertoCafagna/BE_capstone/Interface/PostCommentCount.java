package robertoCafagna.BE_capstone.Interface;

import java.util.UUID;

public interface PostCommentCount {
    UUID getPostId();

    long getCount();
}
