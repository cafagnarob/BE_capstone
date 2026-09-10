package robertoCafagna.BE_capstone.services.SOCIAL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.PostMedia;
import robertoCafagna.BE_capstone.entities.Ride;
import robertoCafagna.BE_capstone.entities.RidePoint;
import robertoCafagna.BE_capstone.enums.MediaType;
import robertoCafagna.BE_capstone.repositories.RIDE.RidePointRepository;
import robertoCafagna.BE_capstone.services.CloudinaryService;
import robertoCafagna.BE_capstone.services.RIDE.MapboxRateLimiter;
import robertoCafagna.BE_capstone.utils.PolylineEncoder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteImageService {

    private static final int MAX_POLYLINE_POINTS = 150;

    private final RidePointRepository ridePointRepository;
    private final MapboxRateLimiter rateLimiter;
    private final CloudinaryService cloudinaryService;
    private final RestClient restClient = RestClient.create();

    @Value("${mapbox.access-token}")
    private String accessToken;

    public PostMedia generateRoutePhoto(Post post, Ride ride, int orderIndex) {
        List<RidePoint> points = ridePointRepository.findByRideIdOrderBySequence(ride.getId());
        if (points.size() < 2) {
            return null;
        }

        List<double[]> reduced = thinPoints(points, MAX_POLYLINE_POINTS);
        String polyline = PolylineEncoder.encode(reduced);

        rateLimiter.checkAndIncrementStaticImages();

        String encodedPath = URLEncoder.encode(polyline, StandardCharsets.UTF_8);
        String imageUrl = "https://api.mapbox.com/styles/v1/mapbox/dark-v11/static/path-4+FF7A2F-0.85("
                + encodedPath + ")/auto/640x400@2x?access_token="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

        byte[] imageBytes;
        try {
            imageBytes = restClient.get().uri(imageUrl).retrieve().body(byte[].class);
        } catch (Exception e) {
            log.error("Errore durante la generazione della foto percorso via Mapbox Static Images", e);
            return null;
        }

        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }

        CloudinaryService.UploadResult result;
        try {
            result = cloudinaryService.uploadImage(imageBytes, "riders-app/posts/route-photos");
        } catch (IOException e) {
            log.warn("Impossibile caricare su Cloudinary la foto percorso generata", e);
            return null;
        }

        PostMedia media = new PostMedia(post, result.url(), MediaType.IMAGE, orderIndex);
        media.setMediaPublicId(result.publicId());
        return media;
    }

    private List<double[]> thinPoints(List<RidePoint> points, int maxPoints) {
        if (points.size() <= maxPoints) {
            return points.stream().map(p -> new double[]{p.getLatitude(), p.getLongitude()}).toList();
        }
        double step = (double) points.size() / maxPoints;
        List<double[]> thinned = new ArrayList<>();
        for (int i = 0; i < maxPoints; i++) {
            RidePoint p = points.get((int) (i * step));
            thinned.add(new double[]{p.getLatitude(), p.getLongitude()});
        }
        return thinned;
    }
}
