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
import robertoCafagna.BE_capstone.DTO.RIDE.RouteResponseDTO;
import robertoCafagna.BE_capstone.config.EventAccessChecker;
import robertoCafagna.BE_capstone.config.RouteMapper;
import robertoCafagna.BE_capstone.entities.*;
import robertoCafagna.BE_capstone.enums.*;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.EVENT.AccessCodeRequestRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.EventInviteRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.services.SOCIAL.NotificationService;
import robertoCafagna.BE_capstone.specifications.EventSpecifications;

import java.time.LocalDateTime;
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
    private final NotificationService notificationService;
    private final AccessCodeRequestRepository accessCodeRequestRepository;
    private final EventInviteRepository eventInviteRepository;

    @Transactional
    public EventDetailDTO createEvent(User organizer, CreateEventRequestDTO body) {
        if (body.visibility() == EventVisibility.PRIVATE_CODE
                && (body.accessCode() == null || body.accessCode().isBlank())) {
            throw new BadRequestException("Specificare un codice di accesso per un evento con visibilità PRIVATE_CODE");
        }

        validateEventTypeConstraints(body.type(), body.routeId(), body.meetingPointLat(), body.meetingPointLng());
        MeetingPoint mp = resolveMeetingPoint(organizer, body.routeId(), body.type(), body.meetingPointLat(), body.meetingPointLng());

        LocalDateTime endDateTime = resolveEndDateTime(body.type(), body.startDateTime(), body.endDateTime(), body.bufferMinutes(), mp.route());

        boolean autoApprove = body.visibility() == EventVisibility.PUBLIC && body.autoApprove();

        Event event = new Event(
                organizer, body.title(), body.description(),
                body.startDateTime(), endDateTime,
                mp.route(), mp.lat(), mp.lng(),
                body.maxParticipants(), body.visibility(),
                body.visibility() == EventVisibility.PRIVATE_CODE ? body.accessCode() : null,
                autoApprove, body.type()
        );

        event.setMeetingPointAddress(mp.address());

        eventRepository.save(event);
        log.info("Utente {} ha creato l'evento {}", organizer.getId(), event.getId());
        return toDetailDTO(organizer, event, 0, false);
    }

    private LocalDateTime resolveEndDateTime(EventType type, LocalDateTime startDateTime, LocalDateTime providedEndDateTime,
                                             Integer bufferMinutes, Route route) {
        return switch (type) {
            case STANDARD -> {
                long bufferSeconds = bufferMinutes != null ? bufferMinutes * 60L : 0;
                yield startDateTime.plusSeconds((long) route.getDurationSeconds() + bufferSeconds);
            }
            case RADUNO -> {
                if (providedEndDateTime == null) {
                    throw new BadRequestException("Un raduno richiede una data di fine");
                }
                if (providedEndDateTime.isBefore(startDateTime)) {
                    throw new BadRequestException("La data di fine non può precedere quella di inizio");
                }
                yield providedEndDateTime;
            }
            case MULTI_DAY_TRIP -> startDateTime; // ricalcolata quando si aggiungono i giorni
        };
    }

    @Transactional
    public EventDetailDTO addDay(User organizer, UUID tripId, AddEventDayRequestDTO body) {
        Event trip = eventRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Viaggio non trovato"));

        if (!trip.getOrganizer().getId().equals(organizer.getId())) {
            throw new ForbiddenException("Non sei l'organizzatore di questo viaggio");
        }
        if (trip.getType() != EventType.MULTI_DAY_TRIP) {
            throw new BadRequestException("Questo evento non è un viaggio multigiorno");
        }
        if (body.type() == EventType.MULTI_DAY_TRIP) {
            throw new BadRequestException("Un giorno non può essere a sua volta un viaggio multigiorno");
        }

        validateEventTypeConstraints(body.type(), body.routeId(), body.meetingPointLat(), body.meetingPointLng());
        MeetingPoint mp = resolveMeetingPoint(organizer, body.routeId(), body.type(), body.meetingPointLat(), body.meetingPointLng());

        LocalDateTime dayEndDateTime = resolveEndDateTime(body.type(), body.startDateTime(), body.endDateTime(), body.bufferMinutes(), mp.route());

        Event day = new Event(
                organizer, body.title(), body.description(),
                body.startDateTime(), dayEndDateTime,
                mp.route(), mp.lat(), mp.lng(),
                0, trip.getVisibility(), null, false, body.type()
        );
        day.setMeetingPointAddress(mp.address());
        day.setParentEvent(trip);

        eventRepository.save(day);

        LocalDateTime maxExisting = trip.getChildren().stream()
                .map(Event::getEndDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(trip.getStartDateTime());
        LocalDateTime newTripEnd = maxExisting.isAfter(dayEndDateTime) ? maxExisting : dayEndDateTime;
        trip.setEndDateTime(newTripEnd);
        eventRepository.save(trip);

        log.info("Aggiunto giorno {} al viaggio {}", day.getId(), tripId);
        return toDetailDTO(organizer, day, 0, false);
    }

    @Transactional
    public EventDetailDTO updateEvent(User currentUser, UUID eventId, UpdateEventRequestDTO body) {
        Event event = getOwnedEvent(currentUser, eventId);

        if (body.title() != null) event.setTitle(body.title());
        if (body.description() != null) event.setDescription(body.description());
        if (body.meetingPointLat() != null) event.setMeetingPointLat(body.meetingPointLat());
        if (body.meetingPointLng() != null) event.setMeetingPointLng(body.meetingPointLng());
        if (body.maxParticipants() != null) event.setMaxParticipants(body.maxParticipants());

        boolean startChanged = body.startDateTime() != null && !body.startDateTime().equals(event.getStartDateTime());
        if (startChanged) {
            event.setStartDateTime(body.startDateTime());
        }

        if (event.getType() == EventType.STANDARD && (startChanged || body.bufferMinutes() != null)) {
            long bufferSeconds = body.bufferMinutes() != null ? body.bufferMinutes() * 60L : 0;
            event.setEndDateTime(event.getStartDateTime().plusSeconds((long) event.getRoute().getDurationSeconds() + bufferSeconds));
        } else if (event.getType() == EventType.RADUNO && body.endDateTime() != null) {
            if (body.endDateTime().isBefore(event.getStartDateTime())) {
                throw new BadRequestException("La data di fine non può precedere quella di inizio");
            }
            event.setEndDateTime(body.endDateTime());
        }

        eventRepository.save(event);
        return toDetailDTO(currentUser, event, countAccepted(eventId), false);
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

        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean hasEnded = event.getEndDateTime().isBefore(LocalDateTime.now());

        if (hasEnded && !isOrganizer && !isAdmin) {
            UUID participationEventId = event.getParentEvent() != null
                    ? event.getParentEvent().getId()
                    : event.getId();

            boolean participated = participationRepository
                    .findByEventIdAndUserId(participationEventId, currentUser.getId())
                    .map(p -> p.getStatus() == ParticipationStatus.ACCEPTED)
                    .orElse(false);

            if (!participated) {
                throw new ForbiddenException("Questo evento si è già concluso e non sei tra i partecipanti");
            }
        }

        if (eventAccessChecker.canSeeDetail(currentUser, event)) {
            return toDetailDTO(currentUser, event, countAccepted(eventId), false);
        }

        if (event.getVisibility() == EventVisibility.PRIVATE_CODE) {
            return toLockedDetailDTO(currentUser, event);
        }

        if (event.getVisibility() == EventVisibility.INVITE_ONLY) {
            boolean hasInvite = eventInviteRepository
                    .findByEventIdAndInvitedUserId(eventId, currentUser.getId())
                    .isPresent();
            if (hasInvite) {
                return toDetailDTO(currentUser, event, countAccepted(eventId), false);
            }
        }

        throw new ForbiddenException("Non hai accesso ai dettagli di questo evento");
    }


    public List<EventSummaryDTO> getPublicEvents(User currentUser) {
        List<Event> events = eventRepository.findByVisibilityInAndStatus(
                List.of(EventVisibility.PUBLIC, EventVisibility.PRIVATE_CODE),
                EventStatus.ACTIVE,
                Pageable.unpaged()
        ).getContent();

        return events.stream().filter(e -> e.getParentEvent() == null)
                .map(e -> toSummaryDTO(currentUser, e)).toList();
    }


    public Page<EventSummaryDTO> searchEvents(User currentUser, EventSearchFilterDTO filters, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;

        List<Specification<Event>> specs = new ArrayList<>();

        specs.add(EventSpecifications.visibilityIn(
                List.of(EventVisibility.PUBLIC, EventVisibility.PRIVATE_CODE)));
        specs.add(EventSpecifications.hasNoParent());
        specs.add(EventSpecifications.hasStatus(EventStatus.ACTIVE));

        if (currentUser.getRole() != Role.ADMIN) {
            specs.add(EventSpecifications.notEnded(LocalDateTime.now()));
        }

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
    public void regenerateAccessCode(User currentUser, UUID eventId, RegenerateAccessCodeRequestDTO body) {
        Event event = getOwnedEvent(currentUser, eventId);

        if (event.getVisibility() != EventVisibility.PRIVATE_CODE) {
            throw new BadRequestException("Questo evento non usa un codice di accesso");
        }
        if (!passwordEncoder.matches(body.currentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Password non corretta");
        }

        event.setAccessCode(body.newAccessCode().trim().toUpperCase());
        eventRepository.save(event);
        log.info("Codice di accesso rigenerato per l'evento {}", eventId);
    }


    public AccessCodeResponseDTO getAccessCode(User currentUser, UUID eventId) {
        Event event = getOwnedEvent(currentUser, eventId);
        if (event.getVisibility() != EventVisibility.PRIVATE_CODE) {
            throw new BadRequestException("Questo evento non usa un codice di accesso");
        }
        return new AccessCodeResponseDTO(event.getAccessCode());
    }


    private Event getOwnedEvent(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei l'organizzatore di questo evento");
        }
        return event;
    }


    private long countAccepted(UUID eventId) {
        return participationRepository.countByEventIdAndStatus(eventId, ParticipationStatus.ACCEPTED);
    }

    public Page<EventSummaryDTO> getOrganizedEvents(User currentUser, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return eventRepository.findByOrganizerIdAndParentEventIsNullOrderByStartDateTimeDesc(currentUser.getId(), pageable)
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


    private void validateEventTypeConstraints(EventType type, UUID routeId, Double lat, Double lng) {
        switch (type) {
            case STANDARD -> {
                if (routeId == null) throw new BadRequestException("Un evento standard richiede un percorso");
            }
            case RADUNO -> {
                if (routeId == null && (lat == null || lng == null)) {
                    throw new BadRequestException("Un raduno richiede un percorso oppure un punto di ritrovo");
                }
            }
            case MULTI_DAY_TRIP -> {
                if (routeId != null) throw new BadRequestException("Un viaggio multigiorno non ha un percorso proprio");
            }
        }
    }

    private MeetingPoint resolveMeetingPoint(User organizer, UUID routeId, EventType type, Double directLat, Double directLng) {
        if (routeId != null) {
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
            if (!route.getCreator().getId().equals(organizer.getId())) {
                throw new ForbiddenException("Non puoi usare un percorso che non hai creato tu");
            }
            if (route.getWaypoints().isEmpty()) {
                throw new BadRequestException("Il percorso selezionato non ha punti validi");
            }
            RouteWaypoint start = route.getWaypoints().get(0);
            String address = reverseGeocodingService.reverseGeocode(start.getLatitude(), start.getLongitude());
            return new MeetingPoint(route, start.getLatitude(), start.getLongitude(), address);
        }
        if (type == EventType.RADUNO) {
            String address = reverseGeocodingService.reverseGeocode(directLat, directLng);
            return new MeetingPoint(null, directLat, directLng, address);
        }
        return new MeetingPoint(null, null, null, null);
    }


    @Transactional
    public void requestAccessCode(User currentUser, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));

        if (event.getVisibility() != EventVisibility.PRIVATE_CODE) {
            throw new BadRequestException("Questo evento non richiede un codice di accesso");
        }
        if (event.getParentEvent() != null) {
            throw new BadRequestException("Richiedi l'accesso dal viaggio completo, non dal singolo giorno");
        }
        if (event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Sei l'organizzatore di questo evento");
        }

        AccessCodeRequest request = accessCodeRequestRepository
                .findByEventIdAndRequesterId(eventId, currentUser.getId())
                .orElse(null);

        if (request != null) {
            if (request.getStatus() != AccessRequestStatus.REJECTED) {
                throw new BadRequestException("Hai già richiesto il codice per questo evento");
            }
            request.setStatus(AccessRequestStatus.PENDING);
            accessCodeRequestRepository.save(request);
        } else {
            request = new AccessCodeRequest(event, currentUser);
            accessCodeRequestRepository.save(request);
        }

        notificationService.notifyAccessCodeRequest(event.getOrganizer(), currentUser, event);
    }

    public List<AccessCodeRequestResponseDTO> getAccessCodeRequests(User organizer, UUID eventId) {
        getOwnedEvent(organizer, eventId);
        return accessCodeRequestRepository.findByEventIdAndStatus(eventId, AccessRequestStatus.PENDING)
                .stream().map(this::toAccessRequestDTO).toList();
    }

    @Transactional
    public void approveAccessCodeRequest(User organizer, UUID eventId, UUID requestId) {
        Event event = getOwnedEvent(organizer, eventId);
        AccessCodeRequest request = accessCodeRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        if (!request.getEvent().getId().equals(eventId)) {
            throw new NotFoundException("Richiesta non trovata per questo evento");
        }
        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new BadRequestException("Questa richiesta è già stata gestita");
        }

        request.setStatus(AccessRequestStatus.APPROVED);
        accessCodeRequestRepository.save(request);
        notificationService.notifyAccessCodeGranted(request.getRequester(), event);
    }

    @Transactional
    public void rejectAccessCodeRequest(User organizer, UUID eventId, UUID requestId) {
        getOwnedEvent(organizer, eventId);
        AccessCodeRequest request = accessCodeRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        if (!request.getEvent().getId().equals(eventId)) {
            throw new NotFoundException("Richiesta non trovata per questo evento");
        }
        if (request.getStatus() != AccessRequestStatus.PENDING) {
            throw new BadRequestException("Questa richiesta è già stata gestita");
        }

        request.setStatus(AccessRequestStatus.REJECTED);
        accessCodeRequestRepository.save(request);
    }

    private AccessCodeRequestResponseDTO toAccessRequestDTO(AccessCodeRequest request) {
        return new AccessCodeRequestResponseDTO(
                request.getId(), request.getRequester().getUsername(),
                request.getRequester().getProfilePicture(), request.getStatus(), request.getCreatedAt()
        );
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
                locked ? null : event.getMeetingPointLng(), event.getType()
        );
    }

    private EventDetailDTO toLockedDetailDTO(User currentUser, Event event) {
        RouteResponseDTO routeSummary = event.getRoute() != null ? new RouteResponseDTO(
                event.getRoute().getId(), event.getRoute().getName(), List.of(), null,
                event.getRoute().getDistanceMeters(), event.getRoute().getDurationSeconds(),
                event.getRoute().isAvoidHighways(), event.getRoute().isAvoidTolls(), event.getRoute().isAvoidFerries(),
                null, event.getRoute().getCreatedAt(), false, true
        ) : null;

        AccessRequestStatus myRequestStatus = accessCodeRequestRepository
                .findByEventIdAndRequesterId(event.getId(), currentUser.getId())
                .map(AccessCodeRequest::getStatus)
                .orElse(null);

        return new EventDetailDTO(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getOrganizer().getUsername(), event.getStartDateTime(), event.getEndDateTime(),
                null, null, null,
                event.getMaxParticipants(), countAccepted(event.getId()),
                event.getVisibility(), event.isAutoApprove(), event.getStatus(), event.getCreatedAt(),
                routeSummary, myStatus(currentUser, event.getId()), false, true,
                event.getType(), null, null, null, null,
                myRequestStatus, null
        );
    }

    private EventDetailDTO toDetailDTO(User currentUser, Event event, long currentParticipants, boolean locked) {
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());

        UUID myInviteId = null;
        if (event.getVisibility() == EventVisibility.INVITE_ONLY && !isOrganizer) {
            myInviteId = eventInviteRepository.findByEventIdAndInvitedUserId(event.getId(), currentUser.getId())
                    .filter(inv -> inv.getStatus() == InviteStatus.PENDING)
                    .map(EventInvite::getId)
                    .orElse(null);
        }


        List<EventSummaryDTO> children = event.getType() == EventType.MULTI_DAY_TRIP
                ? event.getChildren().stream().map(c -> toSummaryDTO(currentUser, c)).toList()
                : null;

        Double totalDistanceMeters = event.getType() == EventType.MULTI_DAY_TRIP
                ? event.getChildren().stream()
                .filter(c -> c.getRoute() != null)
                .mapToDouble(c -> c.getRoute().getDistanceMeters())
                .sum()
                : null;

        return new EventDetailDTO(
                event.getId(), event.getTitle(), event.getDescription(),
                event.getOrganizer().getUsername(), event.getStartDateTime(), event.getEndDateTime(),
                event.getMeetingPointLat(), event.getMeetingPointLng(), event.getMeetingPointAddress(), event.getMaxParticipants(),
                currentParticipants, event.getVisibility(), event.isAutoApprove(),
                event.getStatus(), event.getCreatedAt(),
                event.getRoute() != null ? routeMapper.toDTO(event.getRoute()) : null,
                myStatus(currentUser, event.getId()), isOrganizer, locked,
                event.getType(),
                event.getParentEvent() != null ? event.getParentEvent().getId() : null,
                event.getParentEvent() != null ? event.getParentEvent().getTitle() : null,
                children, totalDistanceMeters, null, myInviteId
        );
    }

    private ParticipationStatus myStatus(User currentUser, UUID eventId) {
        return participationRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .map(Participation::getStatus)
                .orElse(null);
    }

    private record MeetingPoint(Route route, Double lat, Double lng, String address) {
    }
}
