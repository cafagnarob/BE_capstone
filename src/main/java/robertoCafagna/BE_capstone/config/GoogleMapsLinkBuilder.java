package robertoCafagna.BE_capstone.config;

import robertoCafagna.BE_capstone.entities.RouteWaypoint;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleMapsLinkBuilder {
    public static String buildNavigationUrl(List<RouteWaypoint> waypoints) {
        RouteWaypoint origin = waypoints.get(0);
        RouteWaypoint destination = waypoints.get(waypoints.size() - 1);
        List<RouteWaypoint> middle = waypoints.subList(1, waypoints.size() - 1);

        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        url.append("&origin=").append(origin.getLatitude()).append(",").append(origin.getLongitude());
        url.append("&destination=").append(destination.getLatitude()).append(",").append(destination.getLongitude());

        if (!middle.isEmpty()) {
            String waypointsParam = middle.stream()
                    .map(w -> w.getLatitude() + "," + w.getLongitude())
                    .collect(Collectors.joining("|"));
            url.append("&waypoints=").append(URLEncoder.encode(waypointsParam, StandardCharsets.UTF_8));
        }

        url.append("&travelmode=driving");
        return url.toString();
    }
}
