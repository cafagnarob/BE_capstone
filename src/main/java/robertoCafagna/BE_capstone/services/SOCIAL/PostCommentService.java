package robertoCafagna.BE_capstone.services.SOCIAL;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CommentResponseDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CreateCommentRequestDTO;
import robertoCafagna.BE_capstone.Interface.CommentLikeCount;
import robertoCafagna.BE_capstone.Interface.CommentReplyCount;
import robertoCafagna.BE_capstone.entities.CommentLike;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.PostComment;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.CommentLikeRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostCommentRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponseDTO addComment(User currentUser, UUID postId, CreateCommentRequestDTO body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));

        PostComment comment = new PostComment(currentUser, post, body.text());
        postCommentRepository.save(comment);
        notificationService.notifyNewComment(post.getUser(), currentUser, post);
        log.info("Utente {} ha commentato il post {}", currentUser.getId(), postId);
        return toDTO(comment, 0L, false, 0L);
    }

    @Transactional
    public CommentResponseDTO addReply(User currentUser, UUID postId, UUID parentCommentId, CreateCommentRequestDTO body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));
        PostComment parent = postCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Commento non trovato"));

        if (!parent.getPost().getId().equals(postId)) {
            throw new NotFoundException("Commento non trovato per questo post");
        }
        if (parent.getParentComment() != null) {
            throw new BadRequestException("Non puoi rispondere a una risposta, rispondi al commento originale");
        }

        PostComment reply = new PostComment(currentUser, post, body.text());
        reply.setParentComment(parent);
        postCommentRepository.save(reply);
        notificationService.notifyCommentReply(parent.getUser(), currentUser, parent);
        log.info("Utente {} ha risposto al commento {} sul post {}", currentUser.getId(), parentCommentId, postId);
        return toDTO(reply, 0L, false, 0L);
    }

    public Page<CommentResponseDTO> getComments(User currentUser, UUID postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post non trovato");
        }
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size);

        Page<PostComment> comments = postCommentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId, pageable);
        return toDTOPage(currentUser, comments);
    }

    public List<CommentResponseDTO> getReplies(User currentUser, UUID commentId) {
        if (!postCommentRepository.existsById(commentId)) {
            throw new NotFoundException("Commento non trovato");
        }
        List<PostComment> replies = postCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        List<UUID> replyIds = replies.stream().map(PostComment::getId).toList();

        if (replyIds.isEmpty()) return List.of();

        Map<UUID, Long> likeCounts = commentLikeRepository.countByCommentIdIn(replyIds).stream()
                .collect(Collectors.toMap(CommentLikeCount::getCommentId, CommentLikeCount::getCount));
        Set<UUID> likedIds = new HashSet<>(commentLikeRepository.findLikedCommentIds(currentUser.getId(), replyIds));

        return replies.stream()
                .map(r -> toDTO(r, likeCounts.getOrDefault(r.getId(), 0L), likedIds.contains(r.getId()), 0L))
                .toList();
    }

    @Transactional
    public void toggleCommentLike(User currentUser, UUID postId, UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Commento non trovato"));
        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("Commento non trovato per questo post");
        }

        boolean alreadyLiked = commentLikeRepository.existsByUserIdAndCommentId(currentUser.getId(), commentId);
        if (alreadyLiked) {
            commentLikeRepository.deleteByUserIdAndCommentId(currentUser.getId(), commentId);
        } else {
            commentLikeRepository.save(new CommentLike(currentUser, comment));
            notificationService.notifyCommentLike(comment.getUser(), currentUser, comment);
        }
    }

    @Transactional
    public void deleteComment(User currentUser, UUID postId, UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Commento non trovato"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("Commento non trovato per questo post");
        }

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUser.getId());

        if (!isAuthor && !isPostOwner) {
            throw new ForbiddenException("Non puoi eliminare questo commento");
        }

        if (comment.getParentComment() == null) {
            List<PostComment> replies = postCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
            if (!replies.isEmpty()) {
                List<UUID> replyIds = replies.stream().map(PostComment::getId).toList();
                commentLikeRepository.deleteByCommentIdIn(replyIds);
                postCommentRepository.deleteAll(replies);
            }
        }

        commentLikeRepository.deleteByCommentId(commentId);
        postCommentRepository.delete(comment);
        log.info("Commento {} eliminato (utente {})", commentId, currentUser.getId());
    }

    private Page<CommentResponseDTO> toDTOPage(User currentUser, Page<PostComment> comments) {
        List<UUID> commentIds = comments.getContent().stream().map(PostComment::getId).toList();

        if (commentIds.isEmpty()) {
            return comments.map(c -> toDTO(c, 0L, false, 0L));
        }

        Map<UUID, Long> likeCounts = commentLikeRepository.countByCommentIdIn(commentIds).stream()
                .collect(Collectors.toMap(CommentLikeCount::getCommentId, CommentLikeCount::getCount));
        Set<UUID> likedIds = new HashSet<>(commentLikeRepository.findLikedCommentIds(currentUser.getId(), commentIds));
        Map<UUID, Long> replyCounts = postCommentRepository.countRepliesByParentIdIn(commentIds).stream()
                .collect(Collectors.toMap(CommentReplyCount::getCommentId, CommentReplyCount::getCount));

        return comments.map(c -> toDTO(
                c,
                likeCounts.getOrDefault(c.getId(), 0L),
                likedIds.contains(c.getId()),
                replyCounts.getOrDefault(c.getId(), 0L)
        ));
    }

    private CommentResponseDTO toDTO(PostComment comment, long likeCount, boolean liked, long replyCount) {
        return new CommentResponseDTO(
                comment.getId(), comment.getUser().getUsername(), comment.getUser().getProfilePicture(),
                comment.getText(), comment.getCreatedAt(),
                likeCount, liked, replyCount
        );
    }
}
