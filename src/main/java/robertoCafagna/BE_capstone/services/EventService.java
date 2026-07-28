package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.CreateEventRequestDTO;
import robertoCafagna.BE_capstone.DTO.EventDetailDTO;
import robertoCafagna.BE_capstone.DTO.EventSummaryDTO;
import robertoCafagna.BE_capstone.DTO.UpdateEventRequestDTO;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.InviteStatus;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;
import robertoCafagna.BE_capstone.repositories.EventInviteRepository;
import robertoCafagna.BE_capstone.repositories.EventRepository;
import robertoCafagna.BE_capstone.repositories.ParticipationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventInviteRepository eventInviteRepository;


    @Transactional
    public EventDetailDTO createEvent(User organizer, CreateEventRequestDTO body) {
        if (body.endDateTime().isBefore(body.startDateTime())) {
            throw new BadRequestException("La data di fine non può precedere quella di inizio");
        }
        if (body.visibility() == EventVisibility.PRIVATE_CODE
                && (body.accessCode() == null || body.accessCode().isBlank())) {
            throw new BadRequestException("Specificare un codice di accesso per un evento con visibilità PRIVATE_CODE");
        }

        boolean autoApprove = body.visibility() == EventVisibility.PUBLIC && body.autoApprove();

        Event event = new Event(
                organizer, body.title(), body.description(),
                body.startDateTime(), body.endDateTime(),
                body.meetingPointLat(), body.meetingPointLng(),
                body.maxParticipants(), body.visibility(),
                body.visibility() == EventVisibility.PRIVATE_CODE ? body.accessCode() : null,
                autoApprove
        );

        eventRepository.save(event);
        log.info("Utente {} ha creato l'evento {}", organizer.getId(), event.getId());
        return toDetailDTO(event, 0);
    }

    @Transactional
    public EventDetailDTO updateEvent(User currentUser, UUID eventId, UpdateEventRequestDTO body) {
        Event event = getOwnedEvent(currentUser, eventId);

        if (body.title() != null) event.setTitle(body.title());
        if (body.description() != null) event.setDescription(body.description());
        if (body.startDateTime() != null) event.setStartDateTime(body.startDateTime());
        if (body.endDateTime() != null) event.setEndDateTime(body.endDateTime());
        if (body.meetingPointLat() != null) event.setMeetingPointLat(body.meetingPointLat());
        if (body.meetingPointLng() != null) event.setMeetingPointLng(body.meetingPointLng());
        if (body.maxParticipants() != null) event.setMaxParticipants(body.maxParticipants());

        eventRepository.save(event);
        return toDetailDTO(event, countAccepted(eventId));
    }

    @Transactional
    public void changeStatus(User currentUser, UUID eventId, EventStatus newStatus) {
        Event event = getOwnedEvent(currentUser, eventId);

        if (event.getStatus() == EventStatus.FINISHED) {
            throw new BadRequestException("Non è possibile modificare lo stato di un evento già concluso");
        }
        if (event.getStatus() == EventStatus.CANCELLED && newStatus != EventStatus.CANCELLED) {
            throw new BadRequestException("Non è possibile riattivare un evento cancellato");
        }

        event.setStatus(newStatus);
        eventRepository.save(event);
        log.info("Evento {} passato allo stato {}", eventId, newStatus);
    }


    public EventDetailDTO getEventById(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));

        if (!canSeeDetail(currentUser, event)) {
            throw new UnauthorizedException("Non hai accesso ai dettagli di questo evento");
        }

        return toDetailDTO(event, countAccepted(eventId));
    }


    public List<EventSummaryDTO> getPublicEvents(User currentUser) {
        List<Event> events = eventRepository.findByVisibilityInAndStatus(
                List.of(EventVisibility.PUBLIC, EventVisibility.PRIVATE_CODE),
                EventStatus.ACTIVE,
                Pageable.unpaged()
        ).getContent();

        return events.stream().map(e -> toSummaryDTO(currentUser, e)).toList();
    }


    private Event getOwnedEvent(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Non sei l'organizzatore di questo evento");
        }
        return event;
    }

    private boolean canSeeDetail(User currentUser, Event event) {
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


    private long countAccepted(UUID eventId) {
        return participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.ACCEPTED);
    }

    
    private EventSummaryDTO toSummaryDTO(User currentUser, Event event) {
        boolean locked = event.getVisibility() != EventVisibility.PUBLIC && !canSeeDetail(currentUser, event);
        return new EventSummaryDTO(
                event.getId(), event.getTitle(), event.getOrganizer().getUsername(),
                event.getStartDateTime(), event.getMaxParticipants(),
                countAccepted(event.getId()), event.getVisibility(), event.getStatus(), locked
        );
    }

    private EventDetailDTO toDetailDTO(Event event, long currentParticipants) {
        return new EventDetailDTO(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getOrganizer().getUsername(), event.getStartDateTime(), event.getEndDateTime(),
                event.getMeetingPointLat(), event.getMeetingPointLng(), event.getMaxParticipants(),
                currentParticipants, event.getVisibility(), event.isAutoApprove(),
                event.getStatus(), event.getCreatedAt()
        );
    }
}
