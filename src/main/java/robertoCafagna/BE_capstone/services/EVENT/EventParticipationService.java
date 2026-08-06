package robertoCafagna.BE_capstone.services.EVENT;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.EVENT.JoinEventRequestDTO;
import robertoCafagna.BE_capstone.DTO.EVENT.ParticipationResponseDTO;
import robertoCafagna.BE_capstone.config.EventAccessChecker;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.Participation;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;
import robertoCafagna.BE_capstone.services.SOCIAL.NotificationService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventParticipationService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventAccessChecker eventAccessChecker;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Transactional
    public ParticipationResponseDTO join(User currentUser, UUID eventId, JoinEventRequestDTO body) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));

        if (event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Sei l'organizzatore," +
                    " non puoi partecipare al tuo stesso evento");
        }

        participationRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .ifPresent(p -> {
                    throw new BadRequestException("Hai già una richiesta di partecipazione" +
                            " per questo evento");
                });

        if (event.getVisibility() == EventVisibility.INVITE_ONLY) {
            throw new BadRequestException("Questo evento richiede un invito " +
                    "da parte dell'organizzatore");
        }

        long accepted = participationRepository.countByEventIdAndStatus(
                eventId, ParticipationStatus.ACCEPTED);
        if (accepted >= event.getMaxParticipants()) {
            throw new BadRequestException("Numero massimo di partecipanti raggiunto");
        }

        ParticipationStatus initialStatus;
        if (event.getVisibility() == EventVisibility.PRIVATE_CODE) {
            if (body.accessCode() == null || !body.accessCode().trim().equalsIgnoreCase(event.getAccessCode())) {
                throw new BadRequestException("Codice di accesso non valido");
            }
            initialStatus = ParticipationStatus.ACCEPTED;
        } else {
            initialStatus = event.isAutoApprove() ? ParticipationStatus.ACCEPTED : ParticipationStatus.PENDING;
        }

        Participation participation = new Participation(event, currentUser, initialStatus);
        participationRepository.save(participation);
        if (initialStatus == ParticipationStatus.PENDING) {
            notificationService.notifyParticipationRequest(event.getOrganizer(), currentUser, event);
        }
        log.info("Utente {} richiede partecipazione a evento {} (stato: {})",
                currentUser.getId(), eventId, initialStatus);
        return toDTO(participation);
    }

    @Transactional
    public ParticipationResponseDTO approve(User organizer, UUID eventId, UUID participationId) {
        Participation participation = getOwnedParticipation(organizer, eventId, participationId);
        assertPending(participation);

        Event event = participation.getEvent();
        long accepted = participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.ACCEPTED);
        if (accepted >= event.getMaxParticipants()) {
            throw new BadRequestException("Numero massimo di partecipanti raggiunto");
        }

        participation.setStatus(ParticipationStatus.ACCEPTED);
        participationRepository.save(participation);
        notificationService.notifyParticipationAccepted(participation.getUser(), event);
        return toDTO(participation);
    }


    @Transactional
    public ParticipationResponseDTO reject(User organizer, UUID eventId, UUID participationId) {
        Participation participation = getOwnedParticipation(organizer, eventId, participationId);
        assertPending(participation);

        participation.setStatus(ParticipationStatus.REJECTED);
        participationRepository.save(participation);
        notificationService.notifyParticipationRejected(participation.getUser(), participation.getEvent());
        return toDTO(participation);
    }


    @Transactional
    public void cancelMyParticipation(User currentUser, UUID eventId) {
        Participation participation = participationRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Non hai una partecipazione per questo evento"));

        participation.setStatus(ParticipationStatus.CANCELLED);
        participationRepository.save(participation);
    }

    public List<ParticipationResponseDTO> getParticipants(User organizer, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("Non sei l'organizzatore di questo evento");
        }
        return participationRepository.findByEventIdAndStatus(eventId, ParticipationStatus.PENDING)
                .stream().map(this::toDTO).toList();
    }

    public List<ParticipationResponseDTO> getAcceptedParticipants(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));

        if (!eventAccessChecker.canSeeDetail(currentUser, event)) {
            throw new ForbiddenException("Non hai accesso ai partecipanti di questo evento");
        }

        return participationRepository.findByEventIdAndStatus(eventId, ParticipationStatus.ACCEPTED)
                .stream().map(this::toDTO).toList();
    }


    // --- helper ---

    private Participation getOwnedParticipation(User organizer, UUID eventId, UUID participationId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("Richiesta di partecipazione non trovata"));
        if (!participation.getEvent().getId().equals(eventId)
                || !participation.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new ForbiddenException("Non sei l'organizzatore di questo evento");
        }
        return participation;
    }

    private void assertPending(Participation participation) {
        if (participation.getStatus() != ParticipationStatus.PENDING) {
            throw new BadRequestException("Questa richiesta è già stata gestita");
        }
    }

    private ParticipationResponseDTO toDTO(Participation participation) {
        return new ParticipationResponseDTO(
                participation.getId(), participation.getEvent().getId(),
                participation.getUser().getUsername(), participation.getUser().getProfilePicture(),
                participation.getStatus(), participation.getJoinedAt()
        );
    }

}
