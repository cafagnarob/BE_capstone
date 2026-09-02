package robertoCafagna.BE_capstone.initializers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.entities.*;
import robertoCafagna.BE_capstone.enums.*;
import robertoCafagna.BE_capstone.repositories.EVENT.AccessCodeRequestRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.EventInviteRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.EventRepository;
import robertoCafagna.BE_capstone.repositories.EVENT.ParticipationRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.BrandRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.MotorcycleModelRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.VehicleRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RidePointRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RideRepository;
import robertoCafagna.BE_capstone.repositories.RIDE.RouteRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.*;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(50)
public class DataSeeder implements CommandLineRunner {

    private static final String[] FIRST_NAMES = {
            "Marco", "Giulia", "Luca", "Sara", "Andrea", "Chiara", "Davide", "Elena",
            "Matteo", "Francesca", "Simone", "Valentina", "Alessio", "Martina", "Roberto",
            "Federica", "Nicola", "Silvia", "Giovanni", "Alessandra", "Stefano", "Laura",
            "Antonio", "Paola", "Francesco", "Elisa", "Riccardo", "Beatrice", "Lorenzo", "Camilla"
    };
    // --- avatar predefiniti dell'app (stessa lista di PRESET_AVATAR_URLS nel backend/frontend) ---
    private static final String[] PRESET_AVATARS = {
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299175/Senza-titolo-1_0000_Livello-1_ghjr7j.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299175/Senza-titolo-1_0001_Livello-7_d72slm.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299169/Senza-titolo-1_0007_Livello-2_m027er.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299168/Senza-titolo-1_0004_Livello-5_avhx39.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299167/Senza-titolo-1_0006_Livello-3_ilyoal.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299168/Senza-titolo-1_0002_Livello-6_bvr2ia.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299168/Senza-titolo-1_0005_Livello-4_w2qjf4.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788299168/Senza-titolo-1_0003_Livello-8_xmtyzc.png"
    };
    private static final String REAL_PROFILE_PHOTO =
            "https://res.cloudinary.com/ehgscudu/image/upload/v1786348665/riders-app/users/profile/ri7gotayfra4jp3x4vt9.webp";
    private static final String REAL_VEHICLE_PHOTO =
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788270561/riders-app/vehicles/g4zeexsz3ecwxeonf6an.png";
    private static final String[] POST_IMAGES = {
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788269761/riders-app/posts/y3h4rwlgrgieq2hvbbta.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303014/download_1_sblzdu.png",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303107/78796d5327334ae6619ab6df2a04fbd3_mj153s.jpg",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303107/8293b15c67b94edaed079607f9810548_ekplgj.jpg",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303107/668900a90055b3a6a4bf37830f0e8268_ox7ftr.jpg",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303107/b78c137988436d415e058bc601ef8e36_rbskn1.jpg",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303106/c45ba75b9a28f4300c1afa6605cea4d2_ysyjdr.jpg",
            "https://res.cloudinary.com/ehgscudu/image/upload/v1788303105/e43d2e51ce59e0fb8fc8c445579293a3_lmf7la.jpg"
    };
    private static final String[] SOCIAL_COMMENTS = {
            "Che paesaggio pazzesco 😍", "Devo assolutamente farlo anche io questo giro!",
            "Foto fantastica, complimenti!", "Voglio esserci la prossima volta 🔥",
            "Bellissimo, che invidia", "Questo posto è nella mia lista da anni",
            "Giornata perfetta per andare in moto", "Top come sempre 👌",
            "Quanta strada avete fatto in totale?", "Panorama da cartolina"
    };
    private static final String[] BIKE_COMMENTS = {
            "Che bella moto, complimenti per la cura!", "Gli scarichi che sound hanno?",
            "Quanti km ha ora?", "Il colore è pazzesco su questo modello",
            "Sto pensando di prendere la stessa, consigli?", "Manutenzione fatta tutta in officina o fai da te?",
            "Con quella potenza dev'essere un missile in curva", "Bellissima carena, originale?",
            "Quanto consuma in autostrada?", "La preferisco così, senza troppe modifiche"
    };
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final MotorcycleModelRepository motorcycleModelRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventInviteRepository eventInviteRepository;
    private final AccessCodeRequestRepository accessCodeRequestRepository;
    private final RideRepository rideRepository;
    private final RidePointRepository ridePointRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final LikeRepository likeRepository;
    private final FollowingRelationshipRepository followingRelationshipRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    private static String encodePolyline(List<double[]> points) {
        StringBuilder result = new StringBuilder();
        long prevLat = 0, prevLng = 0;
        for (double[] point : points) {
            long lat = Math.round(point[0] * 1e5);
            long lng = Math.round(point[1] * 1e5);
            encodeValue(lat - prevLat, result);
            encodeValue(lng - prevLng, result);
            prevLat = lat;
            prevLng = lng;
        }
        return result.toString();
    }

    // --- USER ---

    private static void encodeValue(long value, StringBuilder result) {
        long v = value < 0 ? ~(value << 1) : (value << 1);
        while (v >= 0x20) {
            result.append((char) ((0x20 | (v & 0x1f)) + 63));
            v >>= 5;
        }
        result.append((char) (v + 63));
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Seeder: il DB contiene già dati, salto il seeding");
            return;
        }

        List<Brand> brands = brandRepository.findAll();
        List<MotorcycleModel> models = motorcycleModelRepository.findAll();

        if (models.isEmpty()) {
            log.warn("Seeder: nessun MotorcycleModel trovato — assicurati che il seeder CSV giri prima di questo");
            return;
        }

        log.info("Seeder da presentazione: avvio popolamento completo ({} brand, {} modelli già presenti)...",
                brands.size(), models.size());

        List<User> users = seedUsers();
        User admin = users.get(0);
        List<Vehicle> vehicles = seedVehicles(users, models);
        List<Route> routes = seedRoutes(users);
        List<Event> events = seedEvents(users, routes);
        seedParticipationsInvitesAndRequests(users, events);
        List<Ride> rides = seedRides(users, vehicles);
        List<Post> posts = seedPosts(users, events, rides, vehicles, routes);
        seedCommentsAndLikes(users, posts);
        seedFollows(users);
        seedProfilesAndLinks(users);
        seedNotifications(users, events, posts);

        log.info("Seeder completato — {} utenti, {} percorsi, {} eventi, {} giri, {} post. Admin: {} / Admin123!",
                users.size(), routes.size(), events.size(), rides.size(), posts.size(), admin.getUsername());
    }

    // --- GARAGE ---

    private String avatarFor(String username) {
        return "https://ui-avatars.com/api/?name=" + username + "&background=random&color=fff&size=200";
    }

    // --- ROUTE: geometria realistica, non una linea retta ---

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();

        User admin = new User("admin", "admin@riderapp.it", passwordEncoder.encode("Admin123!"));
        admin.setName("Admin");
        admin.setSurname("QJ Riders");
        admin.setRole(Role.ADMIN);
        admin.setProfilePicture(REAL_PROFILE_PHOTO);
        admin.setLastLogin(LocalDateTime.now().minusHours(2));
        users.add(admin);

        for (int i = 0; i < FIRST_NAMES.length; i++) {
            String username = (FIRST_NAMES[i] + faker.name().lastName()).toLowerCase().replaceAll("[^a-z0-9]", "") + i;
            User user = new User(username, faker.internet().emailAddress(username), passwordEncoder.encode("Password123!"));
            user.setName(FIRST_NAMES[i]);
            user.setSurname(faker.name().lastName());

            // varietà di avatar: alcuni con foto vera, alcuni con un avatar preimpostato dell'app
            // (utile in demo per mostrare il rendering senza cerchio), il resto generato dalle iniziali
            if (i < 2) {
                user.setProfilePicture(REAL_PROFILE_PHOTO);
            } else if (i < 6) {
                user.setProfilePicture(PRESET_AVATARS[i % PRESET_AVATARS.length]);
            } else {
                user.setProfilePicture(avatarFor(username));
            }

            user.setLastLogin(LocalDateTime.now().minusDays(random.nextInt(45)));
            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    private List<Vehicle> seedVehicles(List<User> users, List<MotorcycleModel> models) {
        List<Vehicle> vehicles = new ArrayList<>();

        // moto dell'admin: modello e foto "vetrina" per la demo
        MotorcycleModel adminModel = models.get(random.nextInt(models.size()));
        Vehicle adminVehicle = new Vehicle(
                users.get(0), adminModel, "La Ufficiale",
                2023, "AD001MI", faker.regexify("[A-Z0-9]{17}"), "Nero Opaco",
                3200, REAL_VEHICLE_PHOTO
        );
        vehicles.add(adminVehicle);

        for (int i = 1; i < users.size(); i++) {
            User owner = users.get(i);
            int vehicleCount = random.nextInt(2) + 1;
            for (int v = 0; v < vehicleCount; v++) {
                MotorcycleModel model = models.get(random.nextInt(models.size()));
                Vehicle vehicle = new Vehicle(
                        owner, model, random.nextBoolean() ? faker.funnyName().name() : null,
                        2015 + random.nextInt(10),
                        faker.regexify("[A-Z]{2}[0-9]{3}[A-Z]{2}"),
                        faker.regexify("[A-Z0-9]{17}"),
                        faker.color().name(),
                        random.nextInt(30000),
                        "https://placehold.co/400x300?text=" + model.getName().replace(" ", "+")
                );
                vehicles.add(vehicle);
            }
        }
        vehicles = vehicleRepository.saveAll(vehicles);

        for (User user : users) {
            vehicles.stream().filter(v -> v.getUser().getId().equals(user.getId())).findFirst()
                    .ifPresent(v -> {
                        user.setCurrentVehicle(v);
                        userRepository.save(user);
                    });
        }
        return vehicles;
    }

    private List<Route> seedRoutes(List<User> users) {
        List<Route> routes = new ArrayList<>();

        routes.add(buildRealisticRoute(users.get(1), "Stelvio Loop", new double[]{46.5286, 10.4527}, new double[]{46.5350, 10.5100}));
        routes.add(buildRealisticRoute(users.get(2), "Costiera Amalfitana Tour", new double[]{40.6340, 14.6027}, new double[]{40.6480, 14.5290}));
        routes.add(buildRealisticRoute(users.get(1), "Appennino Toscano", new double[]{43.7711, 11.2486}, new double[]{43.8200, 11.1800}));
        routes.add(buildRealisticRoute(users.get(3), "Dolomiti Weekend", new double[]{46.4102, 11.8440}, new double[]{46.4600, 11.9200}));
        routes.add(buildRealisticRoute(users.get(2), "Giro del mese scorso", new double[]{45.4642, 9.1900}, new double[]{45.5100, 9.2500}));
        routes.add(buildRealisticRoute(users.get(1), "Viaggio giorno 1 - Roma verso Firenze", new double[]{41.9028, 12.4964}, new double[]{43.7696, 11.2558}));
        routes.add(buildRealisticRoute(users.get(1), "Viaggio giorno 3 - Firenze verso Bologna", new double[]{43.7696, 11.2558}, new double[]{44.4949, 11.3426}));
        routes.add(buildRealisticRoute(users.get(4), "Giro del Garda", new double[]{45.6120, 10.6080}, new double[]{45.7280, 10.6980}));

        // cluster Barletta-Andria-Trani (BAT), tutti entro 40 km
        routes.add(buildRealisticRoute(users.get(0), "Barletta - Castel del Monte", new double[]{41.3181, 16.2810}, new double[]{41.2306, 16.2969}));
        routes.add(buildRealisticRoute(users.get(5), "Alba sull'Adriatico - Trani", new double[]{41.3181, 16.2810}, new double[]{41.2731, 16.4176}));

        // internazionali
        routes.add(buildRealisticRoute(users.get(6), "Nürburgring Nordschleife", new double[]{50.3356, 6.9475}, new double[]{50.3556, 6.9825}));
        routes.add(buildRealisticRoute(users.get(0), "Route Napoléon", new double[]{44.9333, 6.2333}, new double[]{44.0917, 6.2356}));
        routes.add(buildRealisticRoute(users.get(7), "Passo del San Bernardino", new double[]{46.4667, 9.1833}, new double[]{46.3667, 9.2833}));

        return routeRepository.saveAll(routes);
    }

    private Route buildRealisticRoute(User creator, String name, double[] start, double[] end) {
        List<double[]> path = generateWindingPath(start, end, 14);
        String polyline = encodePolyline(path);

        double distanceMeters = 0;
        for (int i = 1; i < path.size(); i++) {
            distanceMeters += haversineDistanceKm(path.get(i - 1), path.get(i)) * 1000;
        }
        double avgSpeedKmH = 45 + random.nextInt(20); // media realistica su strada extraurbana
        double durationSeconds = (distanceMeters / 1000.0) / avgSpeedKmH * 3600;

        Route route = new Route(creator, name, polyline, distanceMeters, durationSeconds,
                random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
        route.setImportable(random.nextBoolean());

        route.addWaypoint(new RouteWaypoint(start[0], start[1], 0, "Partenza"));
        route.addWaypoint(new RouteWaypoint(end[0], end[1], 1, "Arrivo"));

        return route;
    }

    /**
     * Genera un tracciato "serpeggiante" tra due punti: scostamento laterale casuale
     * ma smussato (random walk limitato), che si annulla dolcemente ai due estremi.
     * Non segue vere strade — è una simulazione visiva, non un vero instradamento.
     */
    private List<double[]> generateWindingPath(double[] start, double[] end, int segments) {
        List<double[]> points = new ArrayList<>();
        points.add(start);

        double totalLat = end[0] - start[0];
        double totalLng = end[1] - start[1];
        double length = Math.sqrt(totalLat * totalLat + totalLng * totalLng);
        if (length == 0) length = 0.0001;

        double dirLat = totalLat / length;
        double dirLng = totalLng / length;
        double perpLat = -dirLng;
        double perpLng = dirLat;

        double maxOffset = length * 0.07;
        double prevOffset = 0;

        for (int i = 1; i < segments; i++) {
            double t = (double) i / segments;
            double baseLat = start[0] + totalLat * t;
            double baseLng = start[1] + totalLng * t;

            double delta = (random.nextDouble() - 0.5) * maxOffset * 0.5;
            double offset = Math.max(-maxOffset, Math.min(maxOffset, prevOffset + delta));
            prevOffset = offset;

            double taper = Math.sin(Math.PI * t); // 0 agli estremi, massimo al centro
            double appliedOffset = offset * taper;

            points.add(new double[]{baseLat + perpLat * appliedOffset, baseLng + perpLng * appliedOffset});
        }

        points.add(end);
        return points;
    }

    private double haversineDistanceKm(double[] a, double[] b) {
        final double R = 6371;
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLng = Math.toRadians(b[1] - a[1]);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(b[0]))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.asin(Math.sqrt(h));
    }

    // --- EVENT ---

    private List<Event> seedEvents(List<User> users, List<Route> routes) {
        List<Event> events = new ArrayList<>();
        User admin = users.get(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime christmas = LocalDateTime.of(2026, Month.DECEMBER, 25, 10, 0);
        LocalDateTime newYearsEve = LocalDateTime.of(2026, Month.DECEMBER, 31, 15, 0);

        // 0. STANDARD PUBLIC
        addWaypointEvent(events, users.get(1), "Giro Passo dello Stelvio", now.plusDays(10), now.plusDays(10).plusHours(6),
                routes.get(0), 20, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "Passo dello Stelvio, Bormio (SO)");

        // 1. STANDARD PUBLIC
        addWaypointEvent(events, users.get(2), "Tour Costiera Amalfitana", now.plusDays(15), now.plusDays(15).plusHours(8),
                routes.get(1), 15, EventVisibility.PUBLIC, null, false, EventType.STANDARD, "Piazza Duomo, Amalfi (SA)");

        // 2. STANDARD PRIVATE_CODE
        addWaypointEvent(events, users.get(1), "Giro Appennino Toscano", now.plusDays(20), now.plusDays(20).plusHours(5),
                routes.get(2), 12, EventVisibility.PRIVATE_CODE, "TOSCANA25", false, EventType.STANDARD, "Piazzale Michelangelo, Firenze");

        // 3. STANDARD INVITE_ONLY - admin invitato
        addWaypointEvent(events, users.get(3), "Weekend Dolomiti - Gruppo ristretto", now.plusDays(25), now.plusDays(26),
                routes.get(3), 8, EventVisibility.INVITE_ONLY, null, false, EventType.STANDARD, "Passo Sella, Canazei (TN)");

        // 4. STANDARD PUBLIC concluso
        Event past = addWaypointEvent(events, users.get(2), "Giro concluso del mese scorso", now.minusDays(20), now.minusDays(20).plusHours(4),
                routes.get(4), 10, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "Piazza del Duomo, Milano");
        past.setStatus(EventStatus.FINISHED);

        // 5. RADUNO PUBLIC
        addMeetingPointEvent(events, users.get(3), "Raduno Moto d'Epoca - Piazza Grande", now.plusDays(30), now.plusDays(30).plusHours(10),
                44.8015, 10.3279, 100, EventVisibility.PUBLIC, null, true, EventType.RADUNO, "Piazza Grande, Modena");

        // 6. RADUNO PRIVATE_CODE - admin richiederà il codice
        addMeetingPointEvent(events, users.get(4), "Raduno Club Privato", now.plusDays(12), now.plusDays(12).plusHours(6),
                45.4384, 12.3358, 40, EventVisibility.PRIVATE_CODE, "CLUBVE25", false, EventType.RADUNO, "Piazza San Marco, Venezia");

        // 7. CANCELLED
        Event cancelled = addWaypointEvent(events, users.get(4), "Giro del Garda (annullato per maltempo)", now.plusDays(5), now.plusDays(5).plusHours(4),
                routes.get(7), 15, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "Lungolago, Riva del Garda");
        cancelled.setStatus(EventStatus.CANCELLED);

        // 8. MULTI_DAY_TRIP - padre - admin parteciperà
        Event trip = new Event(users.get(1), "In moto verso Sud - 3 giorni", faker.lorem().paragraph(3),
                now.plusDays(40), now.plusDays(43), null, null, null, 6,
                EventVisibility.PUBLIC, null, false, EventType.MULTI_DAY_TRIP);
        events.add(trip);

        // 9. STANDARD PRIVATE_CODE - organizzato da ADMIN, cluster BAT
        addWaypointEvent(events, admin, "Giro Castel del Monte", now.plusDays(8), now.plusDays(8).plusHours(4),
                routes.get(8), 25, EventVisibility.PRIVATE_CODE, "BAT2026", false, EventType.STANDARD, "Castello di Barletta");

        // 10. STANDARD PUBLIC - admin parteciperà (accettato), cluster BAT
        addWaypointEvent(events, users.get(5), "Alba sull'Adriatico - Trani", now.plusDays(18), now.plusDays(18).plusHours(3),
                routes.get(9), 20, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "Cattedrale di Trani");

        // 11. RADUNO PUBLIC - organizzato da ADMIN, cluster BAT
        addMeetingPointEvent(events, admin, "Disfida di Barletta - Raduno Moto Storiche", now.plusDays(22), now.plusDays(22).plusHours(8),
                41.3181, 16.2810, 80, EventVisibility.PUBLIC, null, true, EventType.RADUNO, "Castello di Barletta");

        // 12. STANDARD PUBLIC - Germania
        addWaypointEvent(events, users.get(6), "Nürburgring Nordschleife Experience", now.plusDays(50), now.plusDays(50).plusHours(6),
                routes.get(10), 18, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "Nürburgring, Nürburg (Germania)");

        // 13. STANDARD INVITE_ONLY - organizzato da ADMIN, Francia
        addWaypointEvent(events, admin, "Route Napoléon", now.plusDays(60), now.plusDays(61),
                routes.get(11), 6, EventVisibility.INVITE_ONLY, null, false, EventType.STANDARD, "Grenoble (Francia)");

        // 14. STANDARD PUBLIC - Svizzera/Italia
        addWaypointEvent(events, users.get(7), "Passo del San Bernardino", now.plusDays(35), now.plusDays(35).plusHours(5),
                routes.get(12), 14, EventVisibility.PUBLIC, null, true, EventType.STANDARD, "San Bernardino (Svizzera)");

        // 15. RADUNO PUBLIC - Natale 2026, organizzato da ADMIN
        addMeetingPointEvent(events, admin, "Motoraduno di Natale - Piazza Bra", christmas, christmas.plusHours(6),
                45.4384, 10.9916, 150, EventVisibility.PUBLIC, null, true, EventType.RADUNO, "Piazza Bra, Verona");

        // 16. STANDARD PUBLIC - Capodanno 2026, admin richiesta in attesa
        addMeetingPointEvent(events, users.get(8), "Giro di Capodanno", newYearsEve, newYearsEve.plusHours(3),
                45.0703, 7.6869, 25, EventVisibility.PUBLIC, null, false, EventType.STANDARD, "Piazza Castello, Torino");

        eventRepository.saveAll(events);

        // giorni del viaggio multigiorno (indice 8) — salvati dopo, serve l'id del padre già persistito
        RouteWaypoint day1Start = routes.get(5).getWaypoints().get(0);
        Event day1 = new Event(users.get(1), "Giorno 1 - Roma-Firenze", faker.lorem().paragraph(1),
                trip.getStartDateTime(), trip.getStartDateTime().plusHours(5),
                routes.get(5), day1Start.getLatitude(), day1Start.getLongitude(), 0,
                trip.getVisibility(), null, false, EventType.STANDARD);
        day1.setMeetingPointAddress("Piazza del Popolo, Roma");
        day1.setParentEvent(trip);

        Event day2 = new Event(users.get(1), "Giorno 2 - Giornata di riposo a Firenze", faker.lorem().paragraph(1),
                trip.getStartDateTime().plusDays(1), trip.getStartDateTime().plusDays(1).plusHours(8),
                null, 43.7696, 11.2558, 0,
                trip.getVisibility(), null, false, EventType.RADUNO);
        day2.setMeetingPointAddress("Piazzale Michelangelo, Firenze");
        day2.setParentEvent(trip);

        RouteWaypoint day3Start = routes.get(6).getWaypoints().get(0);
        Event day3 = new Event(users.get(1), "Giorno 3 - Firenze-Bologna", faker.lorem().paragraph(1),
                trip.getStartDateTime().plusDays(2), trip.getStartDateTime().plusDays(2).plusHours(4),
                routes.get(6), day3Start.getLatitude(), day3Start.getLongitude(), 0,
                trip.getVisibility(), null, false, EventType.STANDARD);
        day3.setMeetingPointAddress("Piazza Maggiore, Bologna");
        day3.setParentEvent(trip);

        eventRepository.saveAll(List.of(day1, day2, day3));

        return events; // i giorni restano fuori: non compaiono in liste/ricerca per progetto
    }

    private Event addWaypointEvent(List<Event> events, User organizer, String title, LocalDateTime start, LocalDateTime end,
                                   Route route, int maxParticipants, EventVisibility visibility, String accessCode,
                                   boolean autoApprove, EventType type, String address) {
        RouteWaypoint wp = route.getWaypoints().get(0);
        Event event = new Event(organizer, title, faker.lorem().paragraph(2), start, end,
                route, wp.getLatitude(), wp.getLongitude(), maxParticipants, visibility, accessCode, autoApprove, type);
        event.setMeetingPointAddress(address);
        events.add(event);
        return event;
    }

    private Event addMeetingPointEvent(List<Event> events, User organizer, String title, LocalDateTime start, LocalDateTime end,
                                       double lat, double lng, int maxParticipants, EventVisibility visibility, String accessCode,
                                       boolean autoApprove, EventType type, String address) {
        Event event = new Event(organizer, title, faker.lorem().paragraph(2), start, end,
                null, lat, lng, maxParticipants, visibility, accessCode, autoApprove, type);
        event.setMeetingPointAddress(address);
        events.add(event);
        return event;
    }

    // --- PARTECIPAZIONI, INVITI, RICHIESTE CODICE ---

    private void seedParticipationsInvitesAndRequests(List<User> users, List<Event> events) {
        User admin = users.get(0);
        List<Participation> participations = new ArrayList<>();

        // Stelvio (0)
        for (int i = 10; i < 15; i++)
            participations.add(new Participation(events.get(0), users.get(i), ParticipationStatus.ACCEPTED));

        // Costiera (1): admin RIFIUTATO qui, per avere quello stato in demo
        participations.add(new Participation(events.get(1), users.get(15), ParticipationStatus.PENDING));
        participations.add(new Participation(events.get(1), users.get(16), ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(1), admin, ParticipationStatus.REJECTED));

        // Toscana PRIVATE_CODE (2)
        participations.add(new Participation(events.get(2), users.get(4), ParticipationStatus.ACCEPTED));

        // Raduno pubblico Modena (5)
        participations.add(new Participation(events.get(5), users.get(6), ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(5), users.get(7), ParticipationStatus.ACCEPTED));

        // Viaggio multigiorno (8): admin ACCETTATO
        participations.add(new Participation(events.get(8), admin, ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(8), users.get(9), ParticipationStatus.PENDING));

        // Giro Castel del Monte, organizzato da admin (9): richieste da approvare
        participations.add(new Participation(events.get(9), users.get(11), ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(9), users.get(12), ParticipationStatus.PENDING));

        // Alba sull'Adriatico Trani (10): admin ACCETTATO
        participations.add(new Participation(events.get(10), admin, ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(10), users.get(13), ParticipationStatus.ACCEPTED));

        // Disfida di Barletta, organizzato da admin (11)
        participations.add(new Participation(events.get(11), users.get(14), ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(11), users.get(17), ParticipationStatus.PENDING));

        // Capodanno (16): admin PENDING
        participations.add(new Participation(events.get(16), admin, ParticipationStatus.PENDING));

        participationRepository.saveAll(participations);

        // inviti — evento INVITE_ONLY (3): admin invitato, in attesa
        List<EventInvite> invites = new ArrayList<>();
        invites.add(new EventInvite(events.get(3), admin));
        EventInvite accepted3 = new EventInvite(events.get(3), users.get(18));
        accepted3.setStatus(InviteStatus.ACCEPTED);
        invites.add(accepted3);
        EventInvite rejected3 = new EventInvite(events.get(3), users.get(19));
        rejected3.setStatus(InviteStatus.REJECTED);
        invites.add(rejected3);

        // inviti — Route Napoléon (13), organizzato da admin
        invites.add(new EventInvite(events.get(13), users.get(20)));
        EventInvite acceptedNap = new EventInvite(events.get(13), users.get(21));
        acceptedNap.setStatus(InviteStatus.ACCEPTED);
        invites.add(acceptedNap);

        eventInviteRepository.saveAll(invites);

        // richieste codice — Toscana (2)
        List<AccessCodeRequest> requests = new ArrayList<>();
        AccessCodeRequest req1 = new AccessCodeRequest(events.get(2), users.get(8));
        req1.setStatus(AccessRequestStatus.APPROVED);
        requests.add(req1);
        requests.add(new AccessCodeRequest(events.get(2), users.get(9)));

        // richieste codice — Raduno Club Privato Venezia (6): ADMIN come richiedente
        AccessCodeRequest adminReq = new AccessCodeRequest(events.get(6), admin);
        adminReq.setStatus(AccessRequestStatus.APPROVED);
        requests.add(adminReq);

        // richieste codice — Giro Castel del Monte (9), organizzato da admin: da approvare
        requests.add(new AccessCodeRequest(events.get(9), users.get(22)));
        AccessCodeRequest rejectedReq = new AccessCodeRequest(events.get(9), users.get(23));
        rejectedReq.setStatus(AccessRequestStatus.REJECTED);
        requests.add(rejectedReq);

        accessCodeRequestRepository.saveAll(requests);
    }

    // --- RIDE ---

    private List<Ride> seedRides(List<User> users, List<Vehicle> vehicles) {
        List<Ride> rides = new ArrayList<>();
        RideType[] types = RideType.values();

        for (Vehicle vehicle : vehicles) {
            int rideCount = 2 + random.nextInt(3); // 2-4 giri per moto
            for (int r = 0; r < rideCount; r++) {
                Ride ride = new Ride(vehicle.getUser(), vehicle, faker.lorem().words(3).toString().replaceAll("[\\[\\],]", ""));
                ride.setType(types[random.nextInt(types.length)]);

                double maxSpeed = 90 + random.nextInt(70);       // 90-159 km/h
                double avgSpeed = maxSpeed * (0.45 + random.nextDouble() * 0.25); // sempre nettamente sotto il massimo
                double distanceKm = 25 + random.nextInt(220);
                int stops = random.nextInt(5);
                int stopSeconds = stops * (120 + random.nextInt(600));

                ride.finishRide(
                        LocalDateTime.now().minusDays(random.nextInt(60)),
                        distanceKm, avgSpeed, maxSpeed, stops, stopSeconds
                );
                rides.add(ride);
            }
        }
        rides = rideRepository.saveAll(rides);

        for (Ride ride : rides) {
            List<RidePoint> points = new ArrayList<>();
            double baseLat = 40.5 + random.nextDouble() * 5.5;
            double baseLng = 8.0 + random.nextDouble() * 8.0;
            for (int i = 0; i < 12; i++) {
                points.add(new RidePoint(
                        ride, baseLat + i * 0.008, baseLng + i * 0.008, i,
                        55.0 + random.nextInt(50), 150.0 + random.nextInt(700),
                        ride.getStartedAt().plusMinutes(i * 4L)
                ));
            }
            ridePointRepository.saveAll(points);
        }
        return rides;
    }

    // --- POST ---

    private List<Post> seedPosts(List<User> users, List<Event> events, List<Ride> rides, List<Vehicle> vehicles, List<Route> routes) {
        List<Post> posts = new ArrayList<>();
        User admin = users.get(0);

        // post "vetrina" dell'admin: uno per ciascun tipo di collegamento
        posts.add(buildPost(admin, "Weekend perfetto in Puglia, si torna presto! 🏍️", events.get(9), null, null,
                adminVehicleOf(vehicles, admin), 0));
        posts.add(buildPost(admin, "Giro di prova con la nuova moto, che soddisfazione", null,
                rides.stream().filter(r -> r.getUser().getId().equals(admin.getId())).findFirst().orElse(null),
                null, adminVehicleOf(vehicles, admin), 1));
        posts.add(buildPost(admin, "Ho salvato questo percorso, chi si aggiunge?", null, null,
                routes.get(routes.size() - 1), null, 2));
        posts.add(buildPost(admin, faker.lorem().paragraph(1), null, null, null, adminVehicleOf(vehicles, admin), 3));

        for (int i = 1; i < users.size(); i++) {
            User author = users.get(i);
            int roll = random.nextInt(4);
            Event event = roll == 1 ? events.get(random.nextInt(events.size())) : null;
            Ride ride = roll == 0 && !rides.isEmpty() ? rides.get(random.nextInt(rides.size())) : null;
            Route route = roll == 2 ? routes.get(random.nextInt(routes.size())) : null;

            Vehicle authorVehicle = vehicles.stream()
                    .filter(v -> v.getUser().getId().equals(author.getId()))
                    .findFirst().orElse(null);
            Vehicle postVehicle = (authorVehicle != null && random.nextBoolean()) ? authorVehicle : null;

            posts.add(buildPost(author, faker.lorem().paragraph(1), event, ride, route, postVehicle, i));
        }

        return postRepository.saveAll(posts);
    }

    private Vehicle adminVehicleOf(List<Vehicle> vehicles, User admin) {
        return vehicles.stream().filter(v -> v.getUser().getId().equals(admin.getId())).findFirst().orElse(null);
    }

    private Post buildPost(User author, String text, Event event, Ride ride, Route route, Vehicle vehicle, int seed) {
        Post post = new Post(author, event, text);
        post.setRide(ride);
        post.setRoute(route);
        post.setVehicle(vehicle);

        List<PostMedia> media = new ArrayList<>();
        media.add(new PostMedia(post, POST_IMAGES[Math.abs(seed) % POST_IMAGES.length], MediaType.IMAGE, 0));
        if (random.nextBoolean()) {
            media.add(new PostMedia(post, POST_IMAGES[Math.abs(seed + 3) % POST_IMAGES.length], MediaType.IMAGE, 1));
        }
        post.setMedia(media);
        return post;
    }

    private void seedCommentsAndLikes(List<User> users, List<Post> posts) {
        List<PostComment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();

        for (Post post : posts) {
            String[] pool = post.getVehicle() != null ? BIKE_COMMENTS : SOCIAL_COMMENTS;
            int commentCount = 1 + random.nextInt(4);
            for (int i = 0; i < commentCount; i++) {
                User commenter = users.get(random.nextInt(users.size()));
                if (commenter.getId().equals(post.getUser().getId())) continue;
                comments.add(new PostComment(commenter, post, pool[random.nextInt(pool.length)]));
            }

            int likeCount = 2 + random.nextInt(10);
            List<User> shuffled = new ArrayList<>(users);
            Collections.shuffle(shuffled);
            for (int i = 0; i < Math.min(likeCount, shuffled.size()); i++) {
                if (!shuffled.get(i).getId().equals(post.getUser().getId())) {
                    likes.add(new Like(shuffled.get(i), post));
                }
            }
        }
        postCommentRepository.saveAll(comments);
        likeRepository.saveAll(likes);
    }

    // --- FOLLOW ---

    private void seedFollows(List<User> users) {
        List<FollowingRelationship> relationships = new ArrayList<>();
        User admin = users.get(0);

        // admin ben seguito e segue un buon numero di persone, per una demo credibile
        for (int i = 1; i < users.size(); i++) {
            if (random.nextInt(3) != 0) relationships.add(new FollowingRelationship(users.get(i), admin));
        }
        for (int i = 1; i < 15; i++) {
            relationships.add(new FollowingRelationship(admin, users.get(i)));
        }

        for (User follower : users) {
            int followCount = 3 + random.nextInt(6);
            for (int i = 0; i < followCount; i++) {
                User target = users.get(random.nextInt(users.size()));
                boolean alreadyExists = relationships.stream().anyMatch(r ->
                        r.getFollower().getId().equals(follower.getId()) && r.getFollowedUser().getId().equals(target.getId()));
                if (!target.getId().equals(follower.getId()) && !alreadyExists) {
                    relationships.add(new FollowingRelationship(follower, target));
                }
            }
        }
        followingRelationshipRepository.saveAll(relationships);
    }

    // --- PROFILO ESTESO ---

    private void seedProfilesAndLinks(List<User> users) {
        User admin = users.get(0);
        UserProfile adminProfile = new UserProfile(
                "Amministratore della community QJ Riders. In sella da sempre 🏍️", "Barletta, Italia",
                LocalDate.of(1990, 5, 12));
        adminProfile.setLocationLat(41.3181);
        adminProfile.setLocationLng(16.2810);
        adminProfile.setUser(admin);
        admin.setProfile(adminProfile);
        adminProfile.addLink(new ProfileLink(Platform.INSTAGRAM, "https://instagram.com/qjriders"));
        adminProfile.addLink(new ProfileLink(Platform.WEBSITE, "https://qjriders.it"));
        userRepository.save(admin);

        for (int i = 1; i < 20 && i < users.size(); i++) {
            User user = users.get(i);
            UserProfile profile = new UserProfile(faker.lorem().sentence(), faker.address().city(),
                    LocalDate.now().minusYears(20 + random.nextInt(30)));
            profile.setUser(user);
            user.setProfile(profile);

            if (random.nextBoolean())
                profile.addLink(new ProfileLink(Platform.INSTAGRAM, "https://instagram.com/" + user.getUsername()));
            if (random.nextBoolean())
                profile.addLink(new ProfileLink(Platform.WEBSITE, "https://" + user.getUsername() + ".it"));
            userRepository.save(user);
        }
    }

    // --- NOTIFICHE ---

    private void seedNotifications(List<User> users, List<Event> events, List<Post> posts) {
        User admin = users.get(0);
        List<Notification> notifications = new ArrayList<>();

        notifications.add(new Notification(admin, users.get(2), NotificationType.FOLLOW,
                users.get(2).getUsername() + " ha iniziato a seguirti", users.get(2).getId(), ReferenceType.USER));

        notifications.add(new Notification(admin, events.get(3).getOrganizer(), NotificationType.EVENT_INVITE,
                "Sei stato invitato all'evento \"" + events.get(3).getTitle() + "\"", events.get(3).getId(), ReferenceType.EVENT));

        notifications.add(new Notification(admin, users.get(11), NotificationType.PARTICIPATION_REQUEST,
                users.get(11).getUsername() + " ha richiesto di partecipare a \"" + events.get(9).getTitle() + "\"",
                events.get(9).getId(), ReferenceType.EVENT));

        notifications.add(new Notification(admin, users.get(5), NotificationType.PARTICIPATION_ACCEPTED,
                "La tua richiesta per \"" + events.get(10).getTitle() + "\" è stata accettata",
                events.get(10).getId(), ReferenceType.EVENT));

        notifications.add(new Notification(admin, users.get(2), NotificationType.PARTICIPATION_REJECTED,
                "La tua richiesta per \"" + events.get(1).getTitle() + "\" è stata rifiutata",
                events.get(1).getId(), ReferenceType.EVENT));

        notifications.add(new Notification(admin, users.get(22), NotificationType.ACCESS_CODE_REQUEST,
                users.get(22).getUsername() + " ha richiesto il codice di accesso per \"" + events.get(9).getTitle() + "\"",
                events.get(9).getId(), ReferenceType.EVENT));

        notifications.add(new Notification(admin, users.get(4), NotificationType.ACCESS_CODE_GRANTED,
                "Il codice per \"" + events.get(6).getTitle() + "\" è " + events.get(6).getAccessCode(),
                events.get(6).getId(), ReferenceType.EVENT));

        List<Post> adminPosts = posts.stream().filter(p -> p.getUser().getId().equals(admin.getId())).toList();
        if (!adminPosts.isEmpty()) {
            Post samplePost = adminPosts.get(0);
            notifications.add(new Notification(admin, users.get(3), NotificationType.LIKE,
                    users.get(3).getUsername() + " ha messo like al tuo post", samplePost.getId(), ReferenceType.POST));
            notifications.add(new Notification(admin, users.get(4), NotificationType.COMMENT,
                    users.get(4).getUsername() + " ha commentato il tuo post", samplePost.getId(), ReferenceType.POST));
        }

        notificationRepository.saveAll(notifications);
    }
}