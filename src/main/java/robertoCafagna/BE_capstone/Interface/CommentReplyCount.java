package robertoCafagna.BE_capstone.Interface;

import java.util.UUID;

public interface CommentReplyCount {
    UUID getCommentId();

    long getCount();
}
