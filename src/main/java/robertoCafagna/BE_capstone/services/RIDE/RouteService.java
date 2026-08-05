package robertoCafagna.BE_capstone.services.RIDE;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.RIDE.*;
import robertoCafagna.BE_capstone.config.RouteMapper;
import robertoCafagna.BE_capstone.entities.Route;
import robertoCafagna.BE_capstone.entities.RouteWaypoint;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.exceptions.UnauthorizedException;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private static final int MAX_ROUTES_PER_USER_PER_DAY = 20;

    private final RouteRepository routeRepository;
    private final MapboxDirectionsService mapboxDirectionsService;
    private final RouteMapper routeMapper;
    private final EventRepository eventRepository;


    @Transactional
    public RouteResponseDTO createRoute(User currentUser, CreateRouteRequestDTO body) {
        // limite per singolo utente
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

        Route route = new Route(currentUser, body.name(), directions.encodedPolyline(),
                directions.distanceMeters(), directions.durationSeconds(),
                body.avoidHighways(), body.avoidTolls(), body.avoidFerries());

        int sequence = 0;
        for (RouteWaypointRequestDTO p : body.points()) {
            route.addWaypoint(new RouteWaypoint(p.latitude(), p.longitude(), sequence++, p.label()));
        }

        routeRepository.save(route);
        log.info("Utente {} ha creato il percorso {}", currentUser.getId(), route.getId());
        return routeMapper.toDTO(route);
    }

    public RouteResponseDTO getRouteById(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
        return routeMapper.toDTO(route);
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
            throw new UnauthorizedException("Non sei il creatore di questo percorso");
        }
        if (eventRepository.existsByRouteId(routeId)) {
            throw new BadRequestException("Non puoi eliminare un percorso usato da uno o più eventi");
        }
        routeRepository.delete(route);
    }


    @Transactional
    public RouteResponseDTO importRoute(User currentUser, UUID routeId) {
        Route original = routeRepository.findByIdWithWaypoints(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));

        if (!original.isImportable()) {
            throw new UnauthorizedException("Il creatore non ha reso importabile questo percorso");
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
            throw new UnauthorizedException("Non sei il creatore di questo percorso");
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

        return new RoutePreviewDTO(
                directions.encodedPolyline(),
                directions.distanceMeters(),
                directions.durationSeconds()
        );
    }

}

