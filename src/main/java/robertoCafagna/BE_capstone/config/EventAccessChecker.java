package robertoCafagna.BE_capstone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.InviteStatus;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;
import robertoCafagna.BE_capstone.repositories.EVENT.EventInviteRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;


@Component
@RequiredArgsConstructor
public class EventAccessChecker {

    private final ParticipationRepository participationRepository;
    private final EventInviteRepository eventInviteRepository;

    public boolean canSeeDetail(User currentUser, Event event) {
        if (event.getVisibility() == EventVisibility.PUBLIC) return true;

        if (event.getOrganizer().getId().equals(currentUser.getId())) return true;

        boolean isAcceptedParticipant = participationRepository
                .findByEventIdAndUserId(event.getId(), currentUser.getId())
                .map(p -> p.getStatus() == ParticipationStatus.ACCEPTED)
                .orElse(false);
        if (isAcceptedParticipant) return true;

        if (event.getVisibility() == EventVisibility.INVITE_ONLY) {
            return eventInviteRepository.findByEventIdAndInvitedUserId(event.getId(), currentUser.getId())
                    .map(i -> i.getStatus() == InviteStatus.ACCEPTED)
                    .orElse(false);
        }

        return false;
    }
}
