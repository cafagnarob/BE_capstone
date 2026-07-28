package robertoCafagna.BE_capstone.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.FollowStatsDTO;
import robertoCafagna.BE_capstone.DTO.FollowUserSummaryDTO;
import robertoCafagna.BE_capstone.entities.FollowingRelationship;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.FollowingRelationshipRepository;
import robertoCafagna.BE_capstone.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowingRelationshipRepository followingRelationshipRepository;
    private final UserRepository userRepository;


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

    public List<FollowUserSummaryDTO> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        return followingRelationshipRepository.findByFollowedUserId(user.getId())
                .stream()
                .map(r -> toSummaryDTO(r.getFollower()))
                .toList();
    }

    public List<FollowUserSummaryDTO> getFollowing(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        return followingRelationshipRepository.findByFollowerId(user.getId())
                .stream()
                .map(r -> toSummaryDTO(r.getFollowedUser()))
                .toList();
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

    private FollowUserSummaryDTO toSummaryDTO(User user) {
        return new FollowUserSummaryDTO(
                user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getProfilePicture()
        );
    }
}
