package robertoCafagna.BE_capstone.services.EVENT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.EVENT.EventInviteResponseDTO;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.EventInvite;
import robertoCafagna.BE_capstone.entities.Participation;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.InviteStatus;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;

import robertoCafagna.BE_capstone.repositories.EVENT.EventInviteRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.SOCIAL.NotificationService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventInviteService {

    private final EventRepository eventRepository;
    private final EventInviteRepository eventInviteRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationService notificationService;


    @Transactional
    public EventInviteResponseDTO invite(User organizer, UUID eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("Non sei l'organizzatore di questo evento");
        }

        User invitedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente " + username + " non trovato"));

        if (invitedUser.getId().equals(organizer.getId())) {
            throw new BadRequestException("Non puoi invitare te stesso");
        }
        if (eventInviteRepository.findByEventIdAndInvitedUserId(eventId, invitedUser.getId()).isPresent()) {
            throw new BadRequestException("Questo utente è già stato invitato");
        }

        EventInvite invite = new EventInvite(event, invitedUser);
        eventInviteRepository.save(invite);
        notificationService.notifyEventInvite(invitedUser, event);
        return toDTO(invite);
    }

    @Transactional
    public EventInviteResponseDTO respond(User currentUser, UUID inviteId, boolean accept) {
        EventInvite invite = eventInviteRepository.findById(inviteId)
                .orElseThrow(() -> new NotFoundException("Invito non trovato"));

        if (!invite.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Questo invito non è per te");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Hai già risposto a questo invito");
        }

        invite.setStatus(accept ? InviteStatus.ACCEPTED : InviteStatus.REJECTED);
        eventInviteRepository.save(invite);

        if (accept) {
            Participation participation = new Participation(invite.getEvent(),
                    currentUser,
                    ParticipationStatus.ACCEPTED);
            participationRepository.save(participation);
        }

        return toDTO(invite);
    }

    public List<EventInviteResponseDTO> getEventInvites(User organizer, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("Non sei l'organizzatore di questo evento");
        }
        return eventInviteRepository.findByEventId(eventId)
                .stream().map(this::toDTO).toList();
    }

    public List<EventInviteResponseDTO> getMyInvites(User currentUser) {
        return eventInviteRepository.findByInvitedUserIdAndStatus(currentUser.getId(), InviteStatus.PENDING)
                .stream().map(this::toDTO).toList();
    }

    private EventInviteResponseDTO toDTO(EventInvite invite) {
        return new EventInviteResponseDTO(
                invite.getId(), invite.getEvent().getId(), invite.getEvent().getTitle(),
                invite.getInvitedUser().getUsername(), invite.getStatus(), invite.getCreatedAt()
        );
    }
}
