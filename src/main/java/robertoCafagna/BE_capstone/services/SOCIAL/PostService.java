package robertoCafagna.BE_capstone.services.SOCIAL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.EVENT.EventSummaryDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.RIDE.RideSummaryDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.*;
import robertoCafagna.BE_capstone.config.EventAccessChecker;
import robertoCafagna.BE_capstone.entities.*;
import robertoCafagna.BE_capstone.enums.FeedType;
import robertoCafagna.BE_capstone.enums.MediaType;
import robertoCafagna.BE_capstone.enums.WidgetType;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.ForbiddenException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.VehicleRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RideRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.LikeRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostCommentRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;
import robertoCafagna.BE_capstone.services.CloudinaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {


    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final RideRepository rideRepository;
    private final LikeRepository likeRepository;
    private final PostCommentRepository postCommentRepository;
    private final CloudinaryService cloudinaryService;
    private final EventAccessChecker eventAccessChecker;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;


    @Transactional
    public PostResponseDTO createPost(User currentUser, CreatePostRequestDTO body, List<MultipartFile> files) {
        Event event = resolveEvent(currentUser, body.eventId());
        Ride ride = resolveRide(currentUser, body.rideId());
        Route route = resolveRoute(currentUser, body.routeId());


        Post post = new Post(currentUser, event, body.text());
        post.setRide(ride);
        post.setRoute(route);

        Vehicle vehicle = resolveVehicle(currentUser, body.vehicleId());

        List<PostMedia> media = buildUploadedMedia(post, files);
        media.addAll(buildRouteMedia(post, ride, body.includeRoutePhoto()));

        if (media.isEmpty()) {
            throw new BadRequestException("Il post deve contenere almeno un'immagine");
        }

        post.setVehicle(vehicle);
        post.setMedia(media);
        postRepository.save(post);
        postRepository.flush();


        if (body.widgets() != null) {
            for (CreatePostWidgetRequestDTO w : body.widgets()) {
                if (w.mediaIndex() < 0 || w.mediaIndex() >= media.size()) {
                    throw new BadRequestException("Indice foto non valido per un widget");
                }
                validateWidgetReference(currentUser, w.type(), w.referenceId());
                post.getWidgets().add(new PostWidget(
                        post, media.get(w.mediaIndex()), w.type(), w.referenceId(), w.size(), w.xPercent(), w.yPercent()
                ));
            }
            postRepository.save(post);
        }

        log.info("Utente {} ha creato il post {}", currentUser.getId(), post.getId());
        return toDTO(currentUser, post);
    }

    private void validateWidgetReference(User currentUser, WidgetType type, UUID referenceId) {
        switch (type) {
            case RIDE -> {
                if (!rideRepository.existsByIdAndUserId(referenceId, currentUser.getId())) {
                    throw new ForbiddenException("Non puoi taggare un giro che non è tuo");
                }
            }
            case ROUTE -> {
                if (!routeRepository.existsByIdAndCreatorId(referenceId, currentUser.getId())) {
                    throw new ForbiddenException("Non puoi taggare un percorso che non è tuo");
                }
            }
            case EVENT -> {
                Event ev = eventRepository.findById(referenceId)
                        .orElseThrow(() -> new NotFoundException("Evento non trovato"));
                if (!eventAccessChecker.canSeeDetail(currentUser, ev)) {
                    throw new ForbiddenException("Non hai accesso a questo evento");
                }
            }
            case VEHICLE -> {
                if (!vehicleRepository.existsByUserIdAndModelId(currentUser.getId(), referenceId)) {
                    throw new ForbiddenException("Non possiedi una moto di questo modello");
                }
            }
        }
    }

    public Page<PostResponseDTO> getFeed(User currentUser, FeedType type, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<Post> posts = switch (type) {
            case FOLLOWING -> postRepository.findFollowingFeed(currentUser.getId(), pageable);
            case EXPLORE -> postRepository.findExploreFeed(currentUser.getId(), pageable);
        };
        return posts.map(p -> toDTO(currentUser, p));
    }

    public Page<PostResponseDTO> getUserPosts(User currentUser, UUID userId, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(p -> toDTO(currentUser, p));
    }

    public PostResponseDTO getPostById(User currentUser, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));
        return toDTO(currentUser, post);
    }

    @Transactional
    public void deletePost(User currentUser, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post non trovato"));
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non sei l'autore di questo post");
        }

        List<String> publicIds = post.getMedia().stream()
                .map(PostMedia::getMediaPublicId)
                .filter(Objects::nonNull)
                .toList();


        // 1. Elimino i like
        likeRepository.deleteByPostId(postId);

        // 2. Elimino i commenti
        postCommentRepository.deleteByPostId(postId);

        // 3. Elimino il post
        postRepository.delete(post);

        for (String publicId : publicIds) {
            try {
                cloudinaryService.deleteImage(publicId);
            } catch (IOException e) {
                log.warn("Impossibile cancellare l'immagine {} del post {}", publicId, postId, e);
            }
        }
        log.info("Utente {} ha eliminato il post {}", currentUser.getId(), postId);
    }

    // --- risoluzione riferimenti opzionali ---

    private Event resolveEvent(User currentUser, UUID eventId) {
        if (eventId == null) return null;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento non trovato"));
        if (!eventAccessChecker.canSeeDetail(currentUser, event)) {
            throw new ForbiddenException("Non hai accesso a questo evento");
        }
        return event;
    }

    private Ride resolveRide(User currentUser, UUID rideId) {
        if (rideId == null) return null;

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Giro non trovato"));
        if (!ride.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non puoi condividere un giro che non è tuo");
        }
        return ride;
    }

    // --- costruzione media ---

    private List<PostMedia> buildUploadedMedia(Post post, List<MultipartFile> files) {
        List<PostMedia> media = new ArrayList<>();
        if (files == null) return media;

        int order = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                CloudinaryService.UploadResult result = cloudinaryService.uploadImage(file, "riders-app/posts");
                PostMedia pm = new PostMedia(post, result.url(), MediaType.IMAGE, order++);
                pm.setMediaPublicId(result.publicId());
                media.add(pm);
            } catch (IOException e) {
                throw new BadRequestException("Errore durante il caricamento di un'immagine");
            }
        }
        return media;
    }

    public Page<PostResponseDTO> getPostsByVehicle(User currentUser, UUID vehicleId, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return postRepository.findByUserIdAndVehicleIdOrderByCreatedAtDesc(currentUser.getId(), vehicleId, pageable)
                .map(p -> toDTO(currentUser, p));
    }

    /**
     * Genera la foto del percorso via Mapbox Static Images API quando l'utente
     * lo richiede su un post collegato a un Ride.
     * <p>
     * TODO: implementare dopo aver definito stile/dimensioni con il frontend pronto.
     */


    private List<PostMedia> buildRouteMedia(Post post, Ride ride, Boolean includeRoutePhoto) {
        boolean wantsRoutePhoto = Boolean.TRUE.equals(includeRoutePhoto);
        return List.of(); // no-op finché Mapbox non è implementato
    }

    private Pageable buildPageable(int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        return PageRequest.of(page, size);
    }

    private Vehicle resolveVehicle(User currentUser, UUID vehicleId) {
        if (vehicleId == null) return null;
        return vehicleRepository.findByIdAndUserId(vehicleId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));
    }

    private Route resolveRoute(User currentUser, UUID routeId) {
        if (routeId == null) return null;
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Percorso non trovato"));
        if (!route.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Non puoi condividere un percorso che non è tuo");
        }
        return route;
    }

    // --- mapping ---

    private PostResponseDTO toDTO(User currentUser, Post post) {
        List<PostMediaResponseDTO> mediaDTOs = post.getMedia().stream()
                .map(m -> new PostMediaResponseDTO(m.getId(), m.getMediaUrl(), m.getType(), m.getOrderIndex()))
                .toList();

        List<PostWidgetResponseDTO> widgetDTOs = post.getWidgets().stream()
                .map(w -> new PostWidgetResponseDTO(
                        w.getId(), w.getMedia().getId(), w.getType(), w.getReferenceId(),
                        w.getSize(), w.getXPercent(), w.getYPercent()
                ))
                .toList();

        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = postCommentRepository.countByPostId(post.getId());
        boolean liked = likeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());

        Route eventRoute = post.getRoute() != null ? post.getRoute()
                : (post.getEvent() != null ? post.getEvent().getRoute() : null);

        return new PostResponseDTO(
                post.getId(), post.getUser().getUsername(), post.getUser().getProfilePicture(),
                post.getText(), post.getCreatedAt(),
                post.getEvent() != null ? toEventSummary(currentUser, post.getEvent()) : null,
                post.getRide() != null ? toRideSummary(post.getRide()) : null,
                post.getVehicle() != null ? toVehicleSummary(post.getVehicle()) : null,
                mediaDTOs, likeCount, commentCount, liked,
                eventRoute != null ? eventRoute.getId() : null,
                eventRoute != null ? eventRoute.getName() : null,
                eventRoute != null ? eventRoute.getDistanceMeters() : null,
                widgetDTOs
        );
    }


    private EventSummaryDTO toEventSummary(User currentUser, Event event) {
        boolean locked = !eventAccessChecker.canSeeDetail(currentUser, event);
        return new EventSummaryDTO(
                event.getId(), event.getTitle(), event.getOrganizer().getUsername(),
                event.getStartDateTime(), event.getMaxParticipants(), 0,
                event.getVisibility(), event.getStatus(), locked, null, false,
                locked ? null : event.getMeetingPointLat(),
                locked ? null : event.getMeetingPointLng(),
                event.getType()
        );
    }

    private RideSummaryDTO toRideSummary(Ride ride) {
        return new RideSummaryDTO(
                ride.getId(), ride.getTitle(), ride.getType(), null,
                ride.getStartedAt(), ride.getEndedAt(), ride.getDistanceKm(), ride.getAvgSpeedKmH(),
                ride.getEndedAt() == null
        );

    }

    private VehicleSummaryDTO toVehicleSummary(Vehicle vehicle) {
        if (vehicle == null) return null;
        return new VehicleSummaryDTO(vehicle.getId(), vehicle.getNickname(), vehicle.getPhotoUrl(), vehicle.getModel().getBrand().getName(),
                vehicle.getModel().getName());
    }
}
