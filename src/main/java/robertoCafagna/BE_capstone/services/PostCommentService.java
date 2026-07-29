package robertoCafagna.BE_capstone.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.CommentResponseDTO;
import robertoCafagna.BE_capstone.DTO.CreateCommentRequestDTO;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.PostComment;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;
import robertoCafagna.BE_capstone.repositories.PostCommentRepository;
import robertoCafagna.BE_capstone.repositories.PostRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponseDTO addComment(User currentUser, UUID postId, CreateCommentRequestDTO body) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));

        PostComment comment = new PostComment(currentUser, post, body.text());
        postCommentRepository.save(comment);
        notificationService.notifyNewComment(post.getUser(), currentUser, post);
        log.info("Utente {} ha commentato il post {}", currentUser.getId(), postId);
        return toDTO(comment);
    }

    public Page<CommentResponseDTO> getComments(UUID postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post non trovato");
        }

        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size);

        return postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
                .map(this::toDTO);
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
            throw new UnauthorizedException("Non puoi eliminare questo commento");
        }

        postCommentRepository.delete(comment);
        log.info("Commento {} eliminato (utente {})", commentId, currentUser.getId());
    }

    private CommentResponseDTO toDTO(PostComment comment) {
        return new CommentResponseDTO(
                comment.getId(), comment.getUser().getUsername(), comment.getUser().getProfilePicture(),
                comment.getText(), comment.getCreatedAt()
        );
    }
}
