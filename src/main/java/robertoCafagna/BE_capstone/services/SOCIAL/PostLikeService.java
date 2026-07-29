package robertoCafagna.BE_capstone.services.SOCIAL;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.SOCIAL.LikeStatusDTO;
import robertoCafagna.BE_capstone.entities.Like;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.LikeRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;


    @Transactional
    public LikeStatusDTO like(User currentUser, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));

        if (likeRepository.existsByUserIdAndPostId(currentUser.getId(), postId)) {
            throw new BadRequestException("Hai già messo like a questo post");
        }

        Like like = new Like(currentUser, post);
        likeRepository.save(like);
        notificationService.notifyNewLike(post.getUser(), currentUser, post);
        log.info("Utente {} ha messo like al post {}", currentUser.getId(), postId);

        return toStatusDTO(postId, currentUser);
    }

    @Transactional
    public LikeStatusDTO unlike(User currentUser, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post non trovato");
        }

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new BadRequestException("Non hai messo like a questo post"));

        likeRepository.delete(like);
        log.info("Utente {} ha rimosso il like dal post {}", currentUser.getId(), postId);

        return toStatusDTO(postId, currentUser);
    }

    public LikeStatusDTO getStatus(User currentUser, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post non trovato");
        }
        return toStatusDTO(postId, currentUser);
    }

    private LikeStatusDTO toStatusDTO(UUID postId, User currentUser) {
        long count = likeRepository.countByPostId(postId);
        boolean liked = likeRepository.existsByUserIdAndPostId(currentUser.getId(), postId);
        return new LikeStatusDTO(count, liked);
    }

}
