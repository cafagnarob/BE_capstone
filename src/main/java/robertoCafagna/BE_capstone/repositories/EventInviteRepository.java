package robertoCafagna.BE_capstone.repositories;

import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.EventInvite;
import robertoCafagna.BE_capstone.enums.InviteStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventInviteRepository {
    Optional<EventInvite> findByEventIdAndInvitedUserId(UUID eventId, UUID invitedUserId);

    List<EventInvite> findByInvitedUserIdAndStatus(UUID invitedUserId, InviteStatus status);

    List<EventInvite> findByEventId(UUID eventId);
}
