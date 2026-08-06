package robertoCafagna.BE_capstone.services.EVENT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.EVENT.*;
import robertoCafagna.BE_capstone.config.EventAccessChecker;
import robertoCafagna.BE_capstone.config.RouteMapper;
import robertoCafagna.BE_capstone.entities.*;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.enums.EventVisibility;
import robertoCafagna.BE_capstone.enums.ParticipationStatus;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.specifications.EventSpecifications;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final RouteMapper routeMapper;
    private final EventAccessChecker eventAccessChecker;
    private final PasswordEncoder passwordEncoder;
    private final RouteRepository routeRepository;
    private final ReverseGeocodingService reverseGeocodingService;


    @Transactional
    public EventDetailDTO createEvent(User organizer, CreateEventRequestDTO body) {
        if (body.endDateTime().isBefore(body.startDateTime())) {
            throw new BadRequestException("La data di fine non può precedere quella di inizio");
        }
        if (body.visibility() == EventVisibility.PRIVATE_CODE
                && (body.accessCode() == null || body.accessCode().isBlank())) {
            throw new BadRequestException("Specificare un codice di accesso per un evento con visibilità PRIVATE_CODE");
        }

        Route route = routeRepository.findById(body.routeId())
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
        if (!route.getCreator().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("Non puoi usare un percorso che non hai creato tu");
        }
        if (route.getWaypoints().isEmpty()) {
            throw new BadRequestException("Il percorso selezionato non ha punti validi");
        }

        RouteWaypoint startPoint = route.getWaypoints().get(0);

        String meetingPointAddress = reverseGeocodingService.reverseGeocode(
                startPoint.getLatitude(), startPoint.getLongitude()
        );

        boolean autoApprove = body.visibility() == EventVisibility.PUBLIC && body.autoApprove();

        Event event = new Event(
                organizer, body.title(), body.description(),
                body.startDateTime(), body.endDateTime(),
                route, startPoint.getLatitude(), startPoint.getLongitude(),
                body.maxParticipants(), body.visibility(),
                body.visibility() == EventVisibility.PRIVATE_CODE ?
                        passwordEncoder.encode(body.accessCode()) : null,
                autoApprove
        );

        event.setMeetingPointAddress(meetingPointAddress);

        eventRepository.save(event);
        log.info("Utente {} ha creato l'evento {}", organizer.getId(), event.getId());
        return toDetailDTO(organizer, event, 0);
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
        return toDetailDTO(currentUser, event, countAccepted(eventId));
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

        if (!eventAccessChecker.canSeeDetail(currentUser, event)) {
            throw new UnauthorizedException("Non hai accesso ai dettagli di questo evento");
        }

        return toDetailDTO(currentUser, event, countAccepted(eventId));
    }


    public List<EventSummaryDTO> getPublicEvents(User currentUser) {
        List<Event> events = eventRepository.findByVisibilityInAndStatus(
                List.of(EventVisibility.PUBLIC, EventVisibility.PRIVATE_CODE),
                EventStatus.ACTIVE,
                Pageable.unpaged()
        ).getContent();

        return events.stream().map(e -> toSummaryDTO(currentUser, e)).toList();
    }


    public Page<EventSummaryDTO> searchEvents(User currentUser, EventSearchFilterDTO filters, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;

        List<Specification<Event>> specs = new ArrayList<>();

        specs.add(EventSpecifications.visibilityIn(
                List.of(EventVisibility.PUBLIC, EventVisibility.PRIVATE_CODE)));
        specs.add(EventSpecifications.hasStatus(EventStatus.ACTIVE));

        if (filters.title() != null && !filters.title().isBlank()) {
            specs.add(EventSpecifications.titleContains(filters.title().trim()));
        }
        if (filters.dateFrom() != null) {
            specs.add(EventSpecifications.startDateAfter(filters.dateFrom()));
        }
        if (filters.dateTo() != null) {
            specs.add(EventSpecifications.startDateBefore(filters.dateTo()));
        }
        if (filters.lat() != null && filters.lng() != null && filters.radiusKm() != null) {
            double cosLat = Math.max(Math.cos(Math.toRadians(filters.lat())), 0.01);
            double deltaLat = filters.radiusKm() / 111.0;
            double deltaLng = filters.radiusKm() / (111.0 * cosLat);

            specs.add(EventSpecifications.withinBoundingBox(
                    filters.lat() - deltaLat, filters.lat() + deltaLat,
                    filters.lng() - deltaLng, filters.lng() + deltaLng
            ));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime"));
        return eventRepository.findAll(Specification.allOf(specs), pageable).map(e -> toSummaryDTO(currentUser, e));
    }


    @Transactional
    public void regenerateAccessCode(User currentUser, UUID eventId, String newAccessCode) {
        Event event = getOwnedEvent(currentUser, eventId);

        if (event.getVisibility() != EventVisibility.PRIVATE_CODE) {
            throw new BadRequestException("Questo evento non usa un codice di accesso");
        }

        event.setAccessCode(passwordEncoder.encode(newAccessCode));
        eventRepository.save(event);
        log.info("Codice di accesso rigenerato per l'evento {}", eventId);
    }


    private Event getOwnedEvent(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Non sei l'organizzatore di questo evento");
        }
        return event;
    }


    private long countAccepted(UUID eventId) {
        return participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.ACCEPTED);
    }

    public Page<EventSummaryDTO> getOrganizedEvents(User currentUser, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return eventRepository.findByOrganizerIdOrderByStartDateTimeDesc(currentUser.getId(), pageable)
                .map(e -> toSummaryDTO(currentUser, e));
    }

    public Page<EventSummaryDTO> getParticipatingEvents(User currentUser, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return eventRepository.findParticipatingEvents(
                currentUser.getId(),
                List.of(ParticipationStatus.PENDING, ParticipationStatus.ACCEPTED),
                pageable
        ).map(e -> toSummaryDTO(currentUser, e));
    }

    private Pageable buildPageable(int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        return PageRequest.of(page, size);
    }


    private EventSummaryDTO toSummaryDTO(User currentUser, Event event) {
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        boolean locked = event.getVisibility() != EventVisibility.PUBLIC
                && !eventAccessChecker.canSeeDetail(currentUser, event);
        return new EventSummaryDTO(
                event.getId(), event.getTitle(), event.getOrganizer().getUsername(),
                event.getStartDateTime(), event.getMaxParticipants(),
                countAccepted(event.getId()), event.getVisibility(), event.getStatus(), locked,
                myStatus(currentUser, event.getId()), isOrganizer, locked ? null : event.getMeetingPointLat(),
                locked ? null : event.getMeetingPointLng()
        );
    }


    private EventDetailDTO toDetailDTO(User currentUser, Event event, long currentParticipants) {
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        return new EventDetailDTO(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getOrganizer().getUsername(), event.getStartDateTime(), event.getEndDateTime(),
                event.getMeetingPointLat(), event.getMeetingPointLng(), event.getMeetingPointAddress(), event.getMaxParticipants(),
                currentParticipants, event.getVisibility(), event.isAutoApprove(),
                event.getStatus(), event.getCreatedAt(), routeMapper.toDTO(event.getRoute()),
                myStatus(currentUser, event.getId()), isOrganizer
        );
    }


    private ParticipationStatus myStatus(User currentUser, UUID eventId) {
        return participationRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .map(Participation::getStatus)
                .orElse(null);
    }
}
