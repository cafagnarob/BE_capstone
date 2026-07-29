package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.ADMIN.ChangePasswordRequestDTO;
import robertoCafagna.BE_capstone.DTO.AUTH.UpdateUsernameRequestDTO;
import robertoCafagna.BE_capstone.DTO.ERROR.UpdateEmailRequestDTO;
import robertoCafagna.BE_capstone.DTO.GARAGE.VehicleSummaryDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.MyProfileResponseDTO;
import robertoCafagna.BE_capstone.DTO.USER.ProfileLinkRequestDTO;
import robertoCafagna.BE_capstone.DTO.USER.ProfileLinkResponseDTO;
import robertoCafagna.BE_capstone.DTO.USER.PublicProfileResponseDTO;
import robertoCafagna.BE_capstone.DTO.USER.UpdateProfileRequestDTO;
import robertoCafagna.BE_capstone.entities.ProfileLink;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.entities.UserProfile;
import robertoCafagna.BE_capstone.entities.Vehicle;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.repositories.GARAGE.VehicleRepository;

import java.io.IOException;
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
            profile.setUser(user);
            user.setProfile(profile);
        } else {
            if (body.description() != null) profile.setDescription(body.description());
            if (body.location() != null) profile.setLocation(body.location());
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


    // --- mapping  ---

    private VehicleSummaryDTO toVehicleSummary(Vehicle vehicle) {
        if (vehicle == null) return null;
        return new VehicleSummaryDTO(vehicle.getId(), vehicle.getNickname(), vehicle.getPhotoUrl());
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

