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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(50)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final MotorcycleModelRepository motorcycleModelRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final EventInviteRepository eventInviteRepository;
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

        log.info("Seeder: DB utenti vuoto, avvio popolamento dati collegati ({} brand, {} modelli già presenti)...",
                brands.size(), models.size());

        List<User> users = seedUsers();
        List<Vehicle> vehicles = seedVehicles(users, models);
        List<Route> routes = seedRoutes(users);
        List<Event> events = seedEvents(users, routes);
        seedParticipationsAndInvites(users, events);
        List<Ride> rides = seedRides(users, vehicles);
        List<Post> posts = seedPosts(users, events, rides, vehicles);
        seedCommentsAndLikes(users, posts);
        seedFollows(users);
        seedProfilesAndLinks(users);
        seedNotifications(users, events);

        log.info("Seeder: completato — {} utenti, {} eventi, {} giri, {} post",
                users.size(), events.size(), rides.size(), posts.size());
    }

    // --- USER ---

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();

        User admin = new User("admin", "admin@riderapp.it", passwordEncoder.encode("Admin123!"));
        admin.setName("Admin");
        admin.setSurname("Sistema");
        admin.setRole(Role.ADMIN);
        users.add(admin);

        for (int i = 0; i < 8; i++) {
            String username = faker.name().username().replace(".", "_") + i;
            User user = new User(username, faker.internet().emailAddress(username), passwordEncoder.encode("Password123!"));
            user.setName(faker.name().firstName());
            user.setSurname(faker.name().lastName());
            user.setLastLogin(LocalDateTime.now().minusDays(random.nextInt(30)));
            users.add(user);
        }

        return userRepository.saveAll(users);
    }


    private List<Vehicle> seedVehicles(List<User> users, List<MotorcycleModel> models) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i < Math.min(6, users.size()); i++) { // salto l'admin (indice 0)
            User owner = users.get(i);
            MotorcycleModel model = models.get(random.nextInt(models.size()));
            Vehicle vehicle = new Vehicle(
                    owner, model, faker.funnyName().name(),
                    2018 + random.nextInt(6),
                    faker.regexify("[A-Z]{2}[0-9]{3}[A-Z]{2}"),
                    faker.regexify("[A-Z0-9]{17}"),
                    faker.color().name(),
                    random.nextInt(20000),
                    "https://placehold.co/400x300?text=Moto"
            );
            vehicles.add(vehicle);
        }
        vehicles = vehicleRepository.saveAll(vehicles);

        // imposto il veicolo attivo per chi ce l'ha
        for (Vehicle v : vehicles) {
            v.getUser().setCurrentVehicle(v);
            userRepository.save(v.getUser());
        }
        return vehicles;
    }

    // --- ROUTE ---

    private List<Route> seedRoutes(List<User> users) {
        List<Route> routes = new ArrayList<>();

        routes.add(buildFakeRoute(users.get(1), "Stelvio Loop",
                new double[]{46.5286, 10.4527}, new double[]{46.5350, 10.5100}));

        routes.add(buildFakeRoute(users.get(2), "Costiera Amalfitana Tour",
                new double[]{40.6340, 14.6027}, new double[]{40.6480, 14.5290}));

        routes.add(buildFakeRoute(users.get(1), "Appennino Toscano",
                new double[]{43.7711, 11.2486}, new double[]{43.8200, 11.1800}));

        routes.add(buildFakeRoute(users.get(3), "Dolomiti Weekend",
                new double[]{46.4102, 11.8440}, new double[]{46.4600, 11.9200}));

        routes.add(buildFakeRoute(users.get(2), "Giro del mese scorso",
                new double[]{45.4642, 9.1900}, new double[]{45.5100, 9.2500}));

        // percorsi usati come "giorni" del viaggio multigiorno (indici 5 e 6)
        routes.add(buildFakeRoute(users.get(1), "Viaggio giorno 1 - Roma verso Firenze",
                new double[]{41.9028, 12.4964}, new double[]{43.7696, 11.2558}));

        routes.add(buildFakeRoute(users.get(1), "Viaggio giorno 3 - Firenze verso Bologna",
                new double[]{43.7696, 11.2558}, new double[]{44.4949, 11.3426}));

        return routeRepository.saveAll(routes);
    }

    private Route buildFakeRoute(User creator, String name, double[] start, double[] end) {

        String fakePolyline = "fake_polyline_" + faker.internet().uuid();
        double distanceMeters = 15000 + random.nextInt(60000);
        double durationSeconds = distanceMeters / 15.0;

        Route route = new Route(creator, name, fakePolyline, distanceMeters, durationSeconds,
                random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
        route.setImportable(random.nextBoolean());

        route.addWaypoint(new RouteWaypoint(start[0], start[1], 0, "Partenza"));
        route.addWaypoint(new RouteWaypoint(end[0], end[1], 1, "Arrivo"));

        return route;
    }


    // --- EVENT ---

    private List<Event> seedEvents(List<User> users, List<Route> routes) {
        List<Event> events = new ArrayList<>();

        RouteWaypoint stelvioStart = routes.get(0).getWaypoints().get(0);
        Event stelvio = new Event(users.get(1), "Giro Passo dello Stelvio", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(6),
                routes.get(0), stelvioStart.getLatitude(), stelvioStart.getLongitude(), 20,
                EventVisibility.PUBLIC, null, true, EventType.STANDARD);
        stelvio.setMeetingPointAddress("Passo dello Stelvio, Bormio (SO)");
        events.add(stelvio);

        RouteWaypoint costieraStart = routes.get(1).getWaypoints().get(0);
        Event costiera = new Event(users.get(2), "Tour Costiera Amalfitana", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(15).plusHours(8),
                routes.get(1), costieraStart.getLatitude(), costieraStart.getLongitude(),
                15, EventVisibility.PUBLIC, null, false, EventType.STANDARD);
        costiera.setMeetingPointAddress("Piazza Duomo, Amalfi (SA)");
        events.add(costiera);

        RouteWaypoint toscanaStart = routes.get(2).getWaypoints().get(0);
        Event toscana = new Event(users.get(1), "Giro Appennino Toscano", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(20).plusHours(5),
                routes.get(2), toscanaStart.getLatitude(), toscanaStart.getLongitude(),
                12, EventVisibility.PRIVATE_CODE, "TOSCANA25", false, EventType.STANDARD);
        toscana.setMeetingPointAddress("Piazzale Michelangelo, Firenze");
        events.add(toscana);

        RouteWaypoint dolomitiStart = routes.get(3).getWaypoints().get(0);
        Event dolomiti = new Event(users.get(3), "Weekend Dolomiti - Gruppo ristretto", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(25), LocalDateTime.now().plusDays(26),
                routes.get(3), dolomitiStart.getLatitude(), dolomitiStart.getLongitude(),
                8, EventVisibility.INVITE_ONLY, null, false, EventType.STANDARD);
        dolomiti.setMeetingPointAddress("Passo Sella, Canazei (TN)");
        events.add(dolomiti);

        RouteWaypoint pastStart = routes.get(4).getWaypoints().get(0);
        Event past = new Event(users.get(2), "Giro concluso del mese scorso", faker.lorem().paragraph(2),
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(20).plusHours(4),
                routes.get(4), pastStart.getLatitude(), pastStart.getLongitude(),
                10, EventVisibility.PUBLIC, null, true, EventType.STANDARD);
        past.setMeetingPointAddress("Piazza del Duomo, Milano");
        past.setStatus(EventStatus.FINISHED);
        events.add(past);

        // --- RADUNO: nessun percorso, solo punto di ritrovo ---
        Event raduno = new Event(users.get(3), "Raduno Moto d'Epoca - Piazza Grande", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(30).plusHours(10),
                null, 44.8015, 10.3279, 100,
                EventVisibility.PUBLIC, null, true, EventType.RADUNO);
        raduno.setMeetingPointAddress("Piazza Grande, Modena");
        events.add(raduno);

        // --- MULTI_DAY_TRIP: evento padre, salvato prima dei suoi giorni ---
        Event trip = new Event(users.get(1), "In moto verso Sud - 3 giorni", faker.lorem().paragraph(3),
                LocalDateTime.now().plusDays(40), LocalDateTime.now().plusDays(43),
                null, null, null, 6,
                EventVisibility.PUBLIC, null, false, EventType.MULTI_DAY_TRIP);
        events.add(trip);

        eventRepository.saveAll(events);

        // i giorni hanno bisogno dell'id del padre già persistito, quindi li salvo in un secondo momento
        RouteWaypoint day1Start = routes.get(5).getWaypoints().get(0);
        Event day1 = new Event(users.get(1), "Giorno 1 - Roma-Firenze", faker.lorem().paragraph(1),
                trip.getStartDateTime(), trip.getStartDateTime().plusHours(5),
                routes.get(5), day1Start.getLatitude(), day1Start.getLongitude(), 0,
                trip.getVisibility(), null, false, EventType.STANDARD);
        day1.setMeetingPointAddress("Piazza del Popolo, Roma");
        day1.setParentEvent(trip);

        // giorno di riposo: nessun percorso, solo ritrovo — dimostra che un giorno può essere un RADUNO
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

        return events; // resta a 7 elementi: i giorni non compaiono qui, coerente col fatto che non sono "listabili"
    }

    private void seedParticipationsAndInvites(List<User> users, List<Event> events) {
        List<Participation> participations = new ArrayList<>();

        for (int i = 4; i < 7; i++) {
            participations.add(new Participation(events.get(0), users.get(i), ParticipationStatus.ACCEPTED));
        }

        participations.add(new Participation(events.get(1), users.get(5), ParticipationStatus.PENDING));
        participations.add(new Participation(events.get(1), users.get(6), ParticipationStatus.ACCEPTED));

        participations.add(new Participation(events.get(2), users.get(4), ParticipationStatus.ACCEPTED));

        // partecipazioni ai nuovi tipi (indici 5 = raduno, 6 = viaggio multigiorno)
        participations.add(new Participation(events.get(5), users.get(4), ParticipationStatus.ACCEPTED));
        participations.add(new Participation(events.get(6), users.get(5), ParticipationStatus.ACCEPTED));

        participationRepository.saveAll(participations);

        List<EventInvite> invites = new ArrayList<>();
        invites.add(new EventInvite(events.get(3), users.get(5)));
        invites.add(new EventInvite(events.get(3), users.get(6)));
        eventInviteRepository.saveAll(invites);
    }

    // --- RIDE ---

    private List<Ride> seedRides(List<User> users, List<Vehicle> vehicles) {
        List<Ride> rides = new ArrayList<>();
        RideType[] types = RideType.values();

        for (Vehicle vehicle : vehicles) {
            Ride ride = new Ride(vehicle.getUser(), vehicle, faker.lorem().words(3).toString().replaceAll("[\\[\\],]", ""));
            ride.setType(types[random.nextInt(types.length)]);
            ride.finishRide(
                    LocalDateTime.now().minusDays(random.nextInt(15)),
                    20.0 + random.nextInt(180),
                    50.0 + random.nextInt(40),
                    80.0 + random.nextInt(60),
                    random.nextInt(4),
                    random.nextInt(1800)
            );
            rides.add(ride);
        }
        rides = rideRepository.saveAll(rides);

        for (Ride ride : rides) {
            List<RidePoint> points = new ArrayList<>();
            double baseLat = 44.0 + random.nextDouble();
            double baseLng = 10.0 + random.nextDouble();
            for (int i = 0; i < 10; i++) {
                points.add(new RidePoint(
                        ride, baseLat + i * 0.01, baseLng + i * 0.01, i,
                        60.0 + random.nextInt(40), 200.0 + random.nextInt(500),
                        ride.getStartedAt().plusMinutes(i * 5L)
                ));
            }
            ridePointRepository.saveAll(points);
        }
        return rides;
    }

    // --- POST ---

    private List<Post> seedPosts(List<User> users, List<Event> events, List<Ride> rides, List<Vehicle> vehicles) {
        List<Post> posts = new ArrayList<>();

        for (int i = 1; i < users.size(); i++) {
            User author = users.get(i);
            Post post = new Post(author, null, faker.lorem().paragraph(1));

            if (!rides.isEmpty() && random.nextBoolean()) {
                post.setRide(rides.get(random.nextInt(rides.size())));
            } else if (random.nextBoolean()) {
                post.setEvent(events.get(random.nextInt(events.size())));
            }

            Vehicle authorVehicle = vehicles.stream()
                    .filter(v -> v.getUser().getId().equals(author.getId()))
                    .findFirst().orElse(null);
            if (authorVehicle != null && random.nextBoolean()) {
                post.setVehicle(authorVehicle);
            }

            List<PostMedia> media = new ArrayList<>();
            media.add(new PostMedia(post, "https://placehold.co/600x400?text=Post+" + i, MediaType.IMAGE, 0));
            post.setMedia(media);

            posts.add(post);
        }
        return postRepository.saveAll(posts);
    }

    private void seedCommentsAndLikes(List<User> users, List<Post> posts) {
        List<PostComment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();

        for (Post post : posts) {
            int commentCount = random.nextInt(3);
            for (int i = 0; i < commentCount; i++) {
                User commenter = users.get(random.nextInt(users.size()));
                comments.add(new PostComment(commenter, post, faker.lorem().sentence()));
            }

            int likeCount = 1 + random.nextInt(4);
            List<User> shuffled = new ArrayList<>(users);
            java.util.Collections.shuffle(shuffled);
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
        for (User follower : users) {
            int followCount = 1 + random.nextInt(3);
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
        for (int i = 1; i < 5 && i < users.size(); i++) {
            User user = users.get(i);
            UserProfile profile = new UserProfile(faker.lorem().sentence(), faker.address().city(),
                    LocalDate.now().minusYears(20 + random.nextInt(30)));
            profile.setUser(user);
            user.setProfile(profile);

            if (random.nextBoolean()) {
                ProfileLink link = new ProfileLink(Platform.INSTAGRAM, "https://instagram.com/" + user.getUsername());
                profile.addLink(link);
            }
            userRepository.save(user);
        }
    }

    // --- NOTIFICHE DI ESEMPIO ---

    private void seedNotifications(List<User> users, List<Event> events) {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(users.get(1), users.get(2), NotificationType.FOLLOW,
                users.get(2).getUsername() + " ha iniziato a seguirti", users.get(2).getId(), ReferenceType.USER));
        notifications.add(new Notification(users.get(1), events.get(3).getOrganizer(), NotificationType.EVENT_INVITE,
                "Sei stato invitato all'evento \"" + events.get(3).getTitle() + "\"", events.get(3).getId(), ReferenceType.EVENT));
        notificationRepository.saveAll(notifications);
    }
}