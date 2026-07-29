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
        List<Event> events = seedEvents(users);
        seedParticipationsAndInvites(users, events);
        List<Ride> rides = seedRides(users, vehicles);
        List<Post> posts = seedPosts(users, events, rides);
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

    // --- EVENT ---

    private List<Event> seedEvents(List<User> users) {
        List<Event> events = new ArrayList<>();

        events.add(new Event(users.get(1), "Giro Passo dello Stelvio", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(6),
                46.5286, 10.4527, 20, EventVisibility.PUBLIC, null, true));

        events.add(new Event(users.get(2), "Tour Costiera Amalfitana", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(15), LocalDateTime.now().plusDays(15).plusHours(8),
                40.6340, 14.6027, 15, EventVisibility.PUBLIC, null, false));

        events.add(new Event(users.get(1), "Giro Appennino Toscano", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(20).plusHours(5),
                43.7711, 11.2486, 12,
                EventVisibility.PRIVATE_CODE, passwordEncoder.encode("TOSCANA25"), false));

        events.add(new Event(users.get(3), "Weekend Dolomiti - Gruppo ristretto", faker.lorem().paragraph(2),
                LocalDateTime.now().plusDays(25), LocalDateTime.now().plusDays(26),
                46.4102, 11.8440, 8, EventVisibility.INVITE_ONLY, null, false));

        events.add(new Event(users.get(2), "Giro concluso del mese scorso", faker.lorem().paragraph(2),
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(20).plusHours(4),
                45.4642, 9.1900, 10, EventVisibility.PUBLIC, null, true));
        events.get(4).setStatus(EventStatus.FINISHED);

        return eventRepository.saveAll(events);
    }

    private void seedParticipationsAndInvites(List<User> users, List<Event> events) {
        List<Participation> participations = new ArrayList<>();

        // Evento pubblico auto-approve (indice 0): 3 partecipanti diretti
        for (int i = 4; i < 7; i++) {
            participations.add(new Participation(events.get(0), users.get(i), ParticipationStatus.ACCEPTED));
        }

        // Evento pubblico senza auto-approve (indice 1): richieste pending + una accettata
        participations.add(new Participation(events.get(1), users.get(5), ParticipationStatus.PENDING));
        participations.add(new Participation(events.get(1), users.get(6), ParticipationStatus.ACCEPTED));

        // Evento con codice (indice 2): partecipante che ha inserito il codice giusto
        participations.add(new Participation(events.get(2), users.get(4), ParticipationStatus.ACCEPTED));

        participationRepository.saveAll(participations);

        // Inviti per l'evento INVITE_ONLY (indice 3)
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

        // punti GPS fittizi per ogni giro (percorso semplice, coordinate incrementali)
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

    private List<Post> seedPosts(List<User> users, List<Event> events, List<Ride> rides) {
        List<Post> posts = new ArrayList<>();

        for (int i = 1; i < users.size(); i++) {
            Post post = new Post(users.get(i), null, faker.lorem().paragraph(1));

            if (!rides.isEmpty() && random.nextBoolean()) {
                post.setRide(rides.get(random.nextInt(rides.size())));
            } else if (random.nextBoolean()) {
                post.setEvent(events.get(random.nextInt(events.size())));
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
        notifications.add(new Notification(users.get(1), NotificationType.FOLLOW,
                users.get(2).getUsername() + " ha iniziato a seguirti", users.get(2).getId(), ReferenceType.USER));
        notifications.add(new Notification(users.get(1), NotificationType.EVENT_INVITE,
                "Sei stato invitato all'evento \"" + events.get(3).getTitle() + "\"", events.get(3).getId(), ReferenceType.EVENT));
        notificationRepository.saveAll(notifications);
    }
}

