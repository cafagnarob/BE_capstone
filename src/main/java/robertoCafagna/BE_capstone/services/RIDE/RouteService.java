package robertoCafagna.BE_capstone.services.RIDE;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.RIDE.*;
import robertoCafagna.BE_capstone.config.RouteMapper;
import robertoCafagna.BE_capstone.entities.Route;
import robertoCafagna.BE_capstone.entities.RouteWaypoint;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.CloudinaryService;
import robertoCafagna.BE_capstone.services.EVENT.EventService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private static final int MAX_ROUTES_PER_USER_PER_DAY = 20;

    private final RouteRepository routeRepository;
    private final MapboxDirectionsService mapboxDirectionsService;
    private final RouteMapper routeMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EventService eventService;
    private final PostRepository postRepository;


    @Transactional
    public RouteResponseDTO createRoute(User currentUser, CreateRouteRequestDTO body, List<MultipartFile> images) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCount = routeRepository.countByCreatorIdAndCreatedAtAfter(currentUser.getId(), startOfDay);
        if (todayCount >= MAX_ROUTES_PER_USER_PER_DAY) {
            throw new BadRequestException("Hai raggiunto il limite giornaliero di percorsi creabili");
        }

        List<double[]> points = body.points().stream()
                .map(p -> new double[]{p.latitude(), p.longitude()})
                .toList();

        MapboxDirectionsService.DirectionsResult directions = mapboxDirectionsService.calculateRoute(
                points, body.avoidHighways(), body.avoidTolls(), body.avoidFerries()
        );

        long totalStopSeconds = body.points().stream()
                .filter(p -> p.stopMinutes() != null)
                .mapToLong(p -> p.stopMinutes() * 60L)
                .sum();

        Route route = new Route(currentUser, body.name(), directions.encodedPolyline(),
                directions.distanceMeters(), directions.durationSeconds() + totalStopSeconds,
                body.avoidHighways(), body.avoidTolls(), body.avoidFerries());

        int sequence = 0;
        for (RouteWaypointRequestDTO p : body.points()) {
            RouteWaypoint waypoint = new RouteWaypoint(p.latitude(), p.longitude(), sequence++, p.label(), p.stopMinutes());

            if (p.imageIndex() != null && images != null && p.imageIndex() >= 0 && p.imageIndex() < images.size()) {
                MultipartFile file = images.get(p.imageIndex());
                if (file != null && !file.isEmpty()) {
                    try {
                        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "riders-app/routes/waypoints");
                        waypoint.setImageUrl(result.url());
                        waypoint.setImagePublicId(result.publicId());
                    } catch (IOException e) {
                        throw new BadRequestException("Errore durante il caricamento di un'immagine del percorso");
                    }
                }
            }

            route.addWaypoint(waypoint);
        }

        routeRepository.save(route);
        log.info("Utente {} ha creato il percorso {}", currentUser.getId(), route.getId());
        return routeMapper.toDTO(route);
    }

    public List<RouteResponseDTO> getImportableRoutesForMap(User currentUser) {
        Pageable pageable = PageRequest.of(0, 50, Sort.by("createdAt").descending());
        return routeRepository.findByImportableTrueAndCreatorIdNot(currentUser.getId(), pageable)
                .stream()
                .filter(r -> !r.getWaypoints().isEmpty())
                .map(r -> toDTO(r, true, false))
                .toList();
    }

    @Transactional
    public RouteResponseDTO updateRoute(User currentUser, UUID routeId, CreateRouteRequestDTO body, List<MultipartFile> images) {
        Route route = routeRepository.findByIdWithWaypoints(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));

        if (!route.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei il creatore di questo percorso");
        }

        double oldDurationSeconds = route.getDurationSeconds();

        List<double[]> points = body.points().stream()
                .map(p -> new double[]{p.latitude(), p.longitude()})
                .toList();

        MapboxDirectionsService.DirectionsResult directions = mapboxDirectionsService.calculateRoute(
                points, body.avoidHighways(), body.avoidTolls(), body.avoidFerries()
        );

        long totalStopSeconds = body.points().stream()
                .filter(p -> p.stopMinutes() != null)
                .mapToLong(p -> p.stopMinutes() * 60L)
                .sum();

        route.setName(body.name());
        route.setEncodedPolyline(directions.encodedPolyline());
        route.setDistanceMeters(directions.distanceMeters());
        route.setDurationSeconds(directions.durationSeconds() + totalStopSeconds);
        route.setAvoidHighways(body.avoidHighways());
        route.setAvoidTolls(body.avoidTolls());
        route.setAvoidFerries(body.avoidFerries());

        Map<UUID, RouteWaypoint> existingById = route.getWaypoints().stream()
                .collect(Collectors.toMap(RouteWaypoint::getId, w -> w));

        Set<UUID> keptIds = new HashSet<>();
        int sequence = 0;

        for (RouteWaypointRequestDTO p : body.points()) {
            RouteWaypoint waypoint;

            if (p.id() != null && existingById.containsKey(p.id())) {
                waypoint = existingById.get(p.id());
                waypoint.setLatitude(p.latitude());
                waypoint.setLongitude(p.longitude());
                waypoint.setLabel(p.label());
                waypoint.setStopMinutes(p.stopMinutes());
                keptIds.add(p.id());
            } else {
                waypoint = new RouteWaypoint(p.latitude(), p.longitude(), sequence, p.label(), p.stopMinutes());
                route.addWaypoint(waypoint);
            }

            waypoint.setSequence(sequence++);

            if (p.imageIndex() != null && images != null && p.imageIndex() >= 0 && p.imageIndex() < images.size()) {
                MultipartFile file = images.get(p.imageIndex());
                if (file != null && !file.isEmpty()) {
                    String oldPublicId = waypoint.getImagePublicId();
                    try {
                        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "riders-app/routes/waypoints");
                        waypoint.setImageUrl(result.url());
                        waypoint.setImagePublicId(result.publicId());
                    } catch (IOException e) {
                        throw new BadRequestException("Errore durante il caricamento di un'immagine del percorso");
                    }
                    if (oldPublicId != null) {
                        try {
                            cloudinaryService.deleteImage(oldPublicId);
                        } catch (IOException e) {
                            log.warn("Impossibile cancellare la vecchia immagine della tappa {}", waypoint.getId(), e);
                        }
                    }
                }
            }
        }

        List<RouteWaypoint> toRemove = existingById.values().stream()
                .filter(w -> !keptIds.contains(w.getId()))
                .toList();

        for (RouteWaypoint removed : toRemove) {
            if (removed.getImagePublicId() != null) {
                try {
                    cloudinaryService.deleteImage(removed.getImagePublicId());
                } catch (IOException e) {
                    log.warn("Impossibile cancellare l'immagine della tappa rimossa {}", removed.getId(), e);
                }
            }
            route.getWaypoints().remove(removed);
        }

        routeRepository.save(route);
        log.info("Utente {} ha modificato il percorso {}", currentUser.getId(), route.getId());

        eventService.handleRouteUpdated(routeId, route.getDurationSeconds(), oldDurationSeconds);

        return routeMapper.toDTO(route);
    }


    @Transactional
    public RouteWaypointResponseDTO updateWaypointImage(User currentUser, UUID routeId, UUID waypointId, MultipartFile image) {
        Route route = routeRepository.findByIdWithWaypoints(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));

        if (!route.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei il creatore di questo percorso");
        }

        RouteWaypoint waypoint = route.getWaypoints().stream()
                .filter(w -> w.getId().equals(waypointId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Tappa non trovata per questo percorso"));

        String oldPublicId = waypoint.getImagePublicId();

        CloudinaryService.UploadResult result;
        try {
            result = cloudinaryService.uploadImage(image, "riders-app/routes/waypoints");
        } catch (IOException e) {
            throw new BadRequestException("Errore durante il caricamento dell'immagine");
        }

        waypoint.setImageUrl(result.url());
        waypoint.setImagePublicId(result.publicId());
        routeRepository.save(route);

        if (oldPublicId != null) {
            try {
                cloudinaryService.deleteImage(oldPublicId);
            } catch (IOException e) {
                log.warn("Impossibile cancellare la vecchia immagine della tappa {}", waypointId, e);
            }
        }

        return new RouteWaypointResponseDTO(
                waypoint.getId(), waypoint.getLatitude(), waypoint.getLongitude(),
                waypoint.getSequence(), waypoint.getLabel(), waypoint.getImageUrl(), waypoint.getStopMinutes()
        );
    }

    public RouteResponseDTO getRouteById(User currentUser, UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));

        boolean isOwner = route.getCreator().getId().equals(currentUser.getId());
        boolean unlocked = isOwner || route.isImportable();

        return toDTO(route, unlocked, isOwner);
    }

    public Page<RouteResponseDTO> getMyRoutes(User currentUser, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size);
        return routeRepository.findByCreatorIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(routeMapper::toDTO);
    }

    @Transactional
    public void deleteRoute(User currentUser, UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
        if (!route.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei il creatore di questo percorso");
        }

        boolean hasUpcomingEvent = eventRepository.findByRouteId(routeId).stream()
                .anyMatch(e -> e.getStatus() == EventStatus.ACTIVE
                        && e.getEndDateTime().isAfter(LocalDateTime.now()));

        if (hasUpcomingEvent) {
            throw new BadRequestException("Non puoi eliminare un percorso usato da un evento non ancora concluso o annullato");
        }

        eventRepository.clearRouteReference(routeId);
        postRepository.clearRouteReference(routeId);
        routeRepository.delete(route);
    }


    @Transactional
    public RouteResponseDTO importRoute(User currentUser, UUID routeId) {
        Route original = routeRepository.findByIdWithWaypoints(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));

        if (!original.isImportable()) {
            throw new ForbiddenException("Il creatore non ha reso importabile questo percorso");
        }

        // copia identica: stessa geometria/distanza/durata, non richiamo di nuovo Mapbox
        Route imported = new Route(
                currentUser,
                original.getName() + " (importato)",
                original.getEncodedPolyline(),
                original.getDistanceMeters(),
                original.getDurationSeconds(),
                original.isAvoidHighways(),
                original.isAvoidTolls(),
                original.isAvoidFerries()
        );

        int sequence = 0;
        for (RouteWaypoint w : original.getWaypoints()) {
            imported.addWaypoint(new RouteWaypoint(w.getLatitude(), w.getLongitude(), sequence++, w.getLabel()));
        }

        routeRepository.save(imported);
        log.info("Utente {} ha importato il percorso {} (originale: {})",
                currentUser.getId(), imported.getId(), original.getId());
        return routeMapper.toDTO(imported);
    }

    @Transactional
    public void setImportable(User currentUser, UUID routeId, boolean value) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
        if (!route.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei il creatore di questo percorso");
        }
        route.setImportable(value);
        routeRepository.save(route);
    }

    public RoutePreviewDTO previewRoute(PreviewRouteRequestDTO body) {
        List<double[]> points = body.points().stream()
                .map(p -> new double[]{p.latitude(), p.longitude()})
                .toList();

        MapboxDirectionsService.DirectionsResult directions = mapboxDirectionsService.calculateRoute(
                points, body.avoidHighways(), body.avoidTolls(), body.avoidFerries()
        );

        long totalStopSeconds = body.points().stream()
                .filter(p -> p.stopMinutes() != null)
                .mapToLong(p -> p.stopMinutes() * 60L)
                .sum();

        return new RoutePreviewDTO(
                directions.encodedPolyline(),
                directions.distanceMeters(),
                directions.durationSeconds() + totalStopSeconds
        );
    }

    public Page<RouteResponseDTO> getUserRoutes(User currentUser, String username, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        boolean isSelf = target.getId().equals(currentUser.getId());

        Page<Route> routes = isSelf
                ? routeRepository.findByCreatorIdOrderByCreatedAtDesc(target.getId(), pageable)
                : routeRepository.findByCreatorIdAndImportableTrueOrderByCreatedAtDesc(target.getId(), pageable);

        return routes.map(r -> toDTO(r, true, isSelf));
    }

    private RouteResponseDTO toDTO(Route route, boolean unlocked, boolean isOwner) {
        RouteResponseDTO full = routeMapper.toDTO(route);

        if (unlocked) {
            return new RouteResponseDTO(
                    full.id(), full.name(), full.waypoints(), full.encodedPolyline(),
                    full.distanceMeters(), full.durationSeconds(),
                    full.avoidHighways(), full.avoidTolls(), full.avoidFerries(),
                    full.googleMapsUrl(), full.createdAt(), full.importable(), false, isOwner
            );
        }

        List<RouteWaypointResponseDTO> strippedWaypoints = full.waypoints().stream()
                .map(wp -> new RouteWaypointResponseDTO(wp.id(), wp.latitude(), wp.longitude(), wp.sequence(), null, null, wp.stopMinutes()))
                .toList();

        return new RouteResponseDTO(
                full.id(), full.name(), strippedWaypoints, full.encodedPolyline(),
                full.distanceMeters(), full.durationSeconds(),
                full.avoidHighways(), full.avoidTolls(), full.avoidFerries(),
                null, full.createdAt(), full.importable(), true, isOwner
        );
    }

}

