package robertoCafagna.BE_capstone.Interface;

import java.util.UUID;

public interface CommentLikeCount {
    UUID getCommentId();

    long getCount();
}
