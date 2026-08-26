package robertoCafagna.BE_capstone.services.SOCIAL;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.SOCIAL.FollowStatsDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.FollowUserSummaryDTO;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.FollowingRelationshipRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {


    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private final FollowingRelationshipRepository followingRelationshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    @Transactional
    public void follow(User currentUser, String targetUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NotFoundException("Utente " + targetUsername + " non trovato"));

        if (target.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Non puoi seguire te stesso");
        }

        if (followingRelationshipRepository.existsByFollowerIdAndFollowedUserId(currentUser.getId(), target.getId())) {
            throw new BadRequestException("Segui già questo utente");
        }

        FollowingRelationship relationship = new FollowingRelationship(currentUser, target);
        followingRelationshipRepository.save(relationship);
        notificationService.notifyNewFollower(target, currentUser);
        log.info("Utente {} ha iniziato a seguire {}", currentUser.getId(), target.getId());
    }

    @Transactional
    public void unfollow(User currentUser, String targetUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NotFoundException("Utente " + targetUsername + " non trovato"));

        FollowingRelationship relationship = followingRelationshipRepository
                .findByFollowerIdAndFollowedUserId(currentUser.getId(), target.getId())
                .orElseThrow(() -> new BadRequestException("Non segui questo utente"));

        followingRelationshipRepository.delete(relationship);
        log.info("Utente {} ha smesso di seguire {}", currentUser.getId(), target.getId());
    }

    public Page<FollowUserSummaryDTO> getFollowers(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        Pageable pageable = buildPageable(page, size);
        return followingRelationshipRepository.findByFollowedUserId(user.getId(), pageable)
                .map(r -> toSummaryDTO(r.getFollower()));
    }

    public Page<FollowUserSummaryDTO> getFollowing(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        Pageable pageable = buildPageable(page, size);
        return followingRelationshipRepository.findByFollowerId(user.getId(), pageable)
                .map(r -> toSummaryDTO(r.getFollowedUser()));
    }


    public FollowStatsDTO getStats(User currentUser, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        long followers = followingRelationshipRepository.countByFollowedUserId(user.getId());
        long following = followingRelationshipRepository.countByFollowerId(user.getId());
        boolean isFollowed = followingRelationshipRepository
                .existsByFollowerIdAndFollowedUserId(currentUser.getId(), user.getId());

        return new FollowStatsDTO(followers, following, isFollowed);
    }

    private Pageable buildPageable(int page, int size) {
        if (size <= 0 || size > MAX_PAGE_SIZE) size = DEFAULT_PAGE_SIZE;
        if (page < 0) page = 0;
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }

    private FollowUserSummaryDTO toSummaryDTO(User user) {
        return new FollowUserSummaryDTO(
                user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getProfilePicture()
        );
    }
}
