package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.*;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.entities.UserProfile;
import robertoCafagna.BE_capstone.entities.Vehicle;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.UserRepository;
import robertoCafagna.BE_capstone.repositories.VehicleRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;


    // --- lettura ---

    public MyProfileResponseDTO getMyProfile(User currentUser) {
        return toMyProfileDTO(currentUser);
    }

    public PublicProfileResponseDTO getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("L'utente " + username + " non è stato trovato"));
        return toPublicProfileDTO(user);
    }


    // --- modifica profilo "social" ---

    @Transactional
    public MyProfileResponseDTO updateProfile(User currentUser, UpdateProfileRequestDTO body) {
        if (body.name() != null) currentUser.setName(body.name());
        if (body.surname() != null) currentUser.setSurname(body.surname());

        UserProfile profile = currentUser.getProfile();
        if (profile == null) {
            profile = new UserProfile(body.description(), body.location(), body.birthDate());
            profile.setUser(currentUser);
            currentUser.setProfile(profile);
        } else {
            if (body.description() != null) profile.setDescription(body.description());
            if (body.location() != null) profile.setLocation(body.location());
            if (body.birthDate() != null) profile.setBirthDate(body.birthDate());
        }

        userRepository.save(currentUser);
        return toMyProfileDTO(currentUser);
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
        if (!passwordEncoder.matches(body.currentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Password non corretta");
        }
        if (body.newUsername().equals(currentUser.getUsername())) {
            throw new BadRequestException("Il nuovo username coincide con quello attuale");
        }
        if (userRepository.existsByUsername(body.newUsername())) {
            throw new BadRequestException("Username già in uso");
        }
        currentUser.setUsername(body.newUsername());
        userRepository.save(currentUser);
        return toMyProfileDTO(currentUser);
    }


    @Transactional
    public MyProfileResponseDTO updateEmail(User currentUser, UpdateEmailRequestDTO body) {
        if (!passwordEncoder.matches(body.currentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Password non corretta");
        }
        if (body.newEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new BadRequestException("La nuova email coincide con quella attuale");
        }
        if (userRepository.existsByEmail(body.newEmail())) {
            throw new BadRequestException("Email già in uso");
        }
        currentUser.setEmail(body.newEmail());
        userRepository.save(currentUser);
        return toMyProfileDTO(currentUser);
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
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));
        currentUser.setCurrentVehicle(vehicle);
        userRepository.save(currentUser);
        return toMyProfileDTO(currentUser);
    }


    // --- mapping privati ---

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
                toVehicleSummary(user.getCurrentVehicle())
        );
    }

    private PublicProfileResponseDTO toPublicProfileDTO(User user) {
        UserProfile profile = user.getProfile();
        return new PublicProfileResponseDTO(
                user.getUsername(), user.getName(), user.getSurname(), user.getProfilePicture(),
                profile != null ? profile.getDescription() : null,
                profile != null ? profile.getLocation() : null,
                toVehicleSummary(user.getCurrentVehicle())
        );
    }


}

