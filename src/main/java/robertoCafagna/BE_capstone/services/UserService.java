package robertoCafagna.BE_capstone.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.ADMIN.ChangePasswordRequestDTO;
import robertoCafagna.BE_capstone.DTO.AUTH.UpdateUsernameRequestDTO;
import robertoCafagna.BE_capstone.DTO.ERROR.UpdateEmailRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.MyProfileResponseDTO;
import robertoCafagna.BE_capstone.DTO.USER.*;
import robertoCafagna.BE_capstone.entities.ProfileLink;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.entities.UserProfile;
import robertoCafagna.BE_capstone.entities.Vehicle;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.GARAGE.VehicleRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private static final int MAX_LINKS_PER_PROFILE = 5;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;


    // --- lettura ---
    @Transactional(readOnly = true)
    public MyProfileResponseDTO getMyProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        return toMyProfileDTO(user);
    }


    @Transactional(readOnly = true)
    public PublicProfileResponseDTO getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("L'utente " + username + " non è stato trovato"));
        return toPublicProfileDTO(user);
    }


    // --- modifica profilo "social" ---

    @Transactional
    public MyProfileResponseDTO updateProfile(User currentUser, UpdateProfileRequestDTO body) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        if (body.name() != null) user.setName(body.name());
        if (body.surname() != null) user.setSurname(body.surname());

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(body.description(), body.location(), body.birthDate());
            profile.setLocationLat(body.locationLat());
            profile.setLocationLng(body.locationLng());
            profile.setUser(user);
            user.setProfile(profile);
        } else {
            if (body.description() != null) profile.setDescription(body.description());
            if (body.location() != null) {
                profile.setLocation(body.location());
                profile.setLocationLat(body.locationLat());
                profile.setLocationLng(body.locationLng());
            }
            if (body.birthDate() != null) profile.setBirthDate(body.birthDate());
        }

        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    // --- credenziali: ognuna con la propria conferma password ---

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequestDTO body) {
        if (!passwordEncoder.matches(body.oldPassword(), currentUser.getPassword())) {
            throw new BadRequestException("La password attuale non è corretta");
        }
        currentUser.setPassword(passwordEncoder.encode(body.newPassword()));
        userRepository.save(currentUser);
    }


    @Transactional
    public MyProfileResponseDTO updateUsername(User currentUser, UpdateUsernameRequestDTO body) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));


        if (!passwordEncoder.matches(body.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Password non corretta");
        }
        if (body.newUsername().equals(user.getUsername())) {
            throw new BadRequestException("Il nuovo username coincide con quello attuale");
        }
        if (userRepository.existsByUsername(body.newUsername())) {
            throw new BadRequestException("Username già in uso");
        }
        user.setUsername(body.newUsername());
        userRepository.save(user);
        return toMyProfileDTO(user);
    }


    @Transactional
    public MyProfileResponseDTO updateEmail(User currentUser, UpdateEmailRequestDTO body) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));


        if (!passwordEncoder.matches(body.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Password non corretta");
        }
        if (body.newEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("La nuova email coincide con quella attuale");
        }
        if (userRepository.existsByEmail(body.newEmail())) {
            throw new BadRequestException("Email già in uso");
        }
        user.setEmail(body.newEmail());
        userRepository.save(user);
        return toMyProfileDTO(user);
    }


    // --- immagine profilo ---

    @Transactional
    public String updateProfilePicture(User currentUser, MultipartFile file) {
        String oldPublicId = currentUser.getProfilePicturePublicId();

        CloudinaryService.UploadResult result;
        try {
            result = cloudinaryService.uploadImage(file, "riders-app/users/profile");
        } catch (IOException e) {
            throw new BadRequestException("Errore durante il caricamento dell'immagine");
        }

        currentUser.setProfilePicture(result.url());
        currentUser.setProfilePicturePublicId(result.publicId());
        userRepository.save(currentUser);

        if (oldPublicId != null) {
            try {
                cloudinaryService.deleteImage(oldPublicId);
            } catch (IOException e) {
                log.warn("Impossibile cancellare la vecchia immagine profilo {} per l'utente {}",
                        oldPublicId, currentUser.getId(), e);
            }
        }

        return result.url();
    }

    // --- account ---

    @Transactional
    public void deactivateAccount(User currentUser) {
        currentUser.setActive(false);
        userRepository.save(currentUser);
    }


    // --- garage ---

    @Transactional
    public MyProfileResponseDTO selectVehicle(User currentUser, UUID vehicleId) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));


        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, user.getId())
                .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));
        user.setCurrentVehicle(vehicle);
        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    @Transactional
    public MyProfileResponseDTO addProfileLink(User currentUser, ProfileLinkRequestDTO body) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(null, null, null);
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (profile.getLinks().size() >= MAX_LINKS_PER_PROFILE) {
            throw new BadRequestException("Puoi aggiungere al massimo " + MAX_LINKS_PER_PROFILE + " link");
        }

        boolean alreadyExists = profile.getLinks().stream()
                .anyMatch(l -> l.getPlatform() == body.platform());
        if (alreadyExists) {
            throw new BadRequestException("Hai già un link per questa piattaforma, modificalo invece di aggiungerne uno nuovo");
        }

        ProfileLink link = new ProfileLink(body.platform(), body.url());
        profile.addLink(link);

        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    @Transactional
    public MyProfileResponseDTO updateProfileLink(User currentUser, UUID linkId, ProfileLinkRequestDTO body) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        ProfileLink link = getOwnedLink(user, linkId);
        link.setPlatform(body.platform());
        link.setUrl(body.url());

        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    @Transactional
    public MyProfileResponseDTO deleteProfileLink(User currentUser, UUID linkId) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        UserProfile profile = user.getProfile();
        ProfileLink link = getOwnedLink(user, linkId);
        profile.removeLink(link);

        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    private ProfileLink getOwnedLink(User currentUser, UUID linkId) {
        UserProfile profile = currentUser.getProfile();
        if (profile == null) {
            throw new NotFoundException("Non hai ancora un profilo con link");
        }
        return profile.getLinks().stream()
                .filter(l -> l.getId().equals(linkId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Link non trovato"));
    }

    @Transactional
    public MyProfileResponseDTO clearCurrentVehicle(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        user.setCurrentVehicle(null);
        userRepository.save(user);
        return toMyProfileDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserSearchResultDTO> searchUsers(User currentUser, String query, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;

        if (query == null || query.isBlank()) {
            return Page.empty(PageRequest.of(page, size));
        }

        User me = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        UserProfile myProfile = me.getProfile();

        if (myProfile == null || myProfile.getLocationLat() == null || myProfile.getLocationLng() == null) {
            // nessuna posizione nota: comportamento invariato, ordinamento e paginazione a database
            Pageable pageable = PageRequest.of(page, size);
            return userRepository.searchActive(query.trim(), pageable).map(this::toSearchResultDTO);
        }

        double myLat = myProfile.getLocationLat();
        double myLng = myProfile.getLocationLng();

        // limite di sicurezza: raccolgo un insieme ampio ma non illimitato di corrispondenze testuali,
        // poi ordino per vicinanza in memoria e pagino a mano — corretto a questa scala, non pensato per milioni di utenti
        List<User> matches = userRepository.searchActive(query.trim(), PageRequest.of(0, 500)).getContent();

        List<User> sorted = matches.stream()
                .sorted(Comparator.comparingDouble(u -> distanceOrMax(u, myLat, myLng)))
                .toList();

        int from = Math.min(page * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        List<UserSearchResultDTO> content = sorted.subList(from, to).stream()
                .map(this::toSearchResultDTO)
                .toList();

        return new PageImpl<>(content, PageRequest.of(page, size), sorted.size());
    }

    private double distanceOrMax(User u, double lat, double lng) {
        UserProfile p = u.getProfile();
        if (p == null || p.getLocationLat() == null || p.getLocationLng() == null) {
            return Double.MAX_VALUE; // chi non ha una posizione va in fondo alla lista, non sparisce
        }
        return haversineKm(lat, lng, p.getLocationLat(), p.getLocationLng());
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private UserSearchResultDTO toSearchResultDTO(User u) {
        return new UserSearchResultDTO(u.getId(), u.getUsername(), u.getName(), u.getSurname(), u.getProfilePicture());
    }


    // --- mapping  ---

    private VehicleSummaryDTO toVehicleSummary(Vehicle vehicle) {
        if (vehicle == null) return null;
        return new VehicleSummaryDTO(vehicle.getId(), vehicle.getNickname(), vehicle.getPhotoUrl(), vehicle.getModel().getBrand().getName(),
                vehicle.getModel().getName());
    }

    private MyProfileResponseDTO toMyProfileDTO(User user) {
        UserProfile profile = user.getProfile();
        return new MyProfileResponseDTO(
                user.getId(), user.getUsername(), user.getName(), user.getSurname(), user.getEmail(),
                user.getProfilePicture(),
                profile != null ? profile.getDescription() : null,
                profile != null ? profile.getLocation() : null,
                profile != null ? profile.getBirthDate() : null,
                user.getCreatedAt(), user.getLastLogin(), user.isActive(),
                toVehicleSummary(user.getCurrentVehicle()),
                toLinkDTOs(profile)
        );
    }

    private PublicProfileResponseDTO toPublicProfileDTO(User user) {
        UserProfile profile = user.getProfile();
        return new PublicProfileResponseDTO(
                user.getId(),
                user.getUsername(), user.getName(), user.getSurname(), user.getProfilePicture(),
                profile != null ? profile.getDescription() : null,
                profile != null ? profile.getLocation() : null,
                toVehicleSummary(user.getCurrentVehicle()),
                toLinkDTOs(profile)
        );
    }

    private List<ProfileLinkResponseDTO> toLinkDTOs(UserProfile profile) {
        if (profile == null) return List.of();
        return profile.getLinks().stream()
                .map(l -> new ProfileLinkResponseDTO(l.getId(), l.getPlatform(), l.getUrl()))
                .toList();
    }


}

