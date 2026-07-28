package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.DTO.*;
import robertoCafagna.BE_capstone.entities.MotorcycleModel;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.entities.Vehicle;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.MotorcycleModelRepository;
import robertoCafagna.BE_capstone.repositories.UserRepository;
import robertoCafagna.BE_capstone.repositories.VehicleRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final MotorcycleModelRepository motorcycleModelRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;


    @Transactional
    public VehicleResponseDTO addVehicle(User currentUser, CreateVehicleRequestDTO body, MultipartFile photo) {
        MotorcycleModel model = motorcycleModelRepository.findById(body.modelId())
                .orElseThrow(() -> new NotFoundException("Modello non trovato"));

        if (body.licensePlate() != null && vehicleRepository.existsByLicensePlate(body.licensePlate())) {
            throw new BadRequestException("Targa già registrata");
        }

        String photoUrl = null;
        String photoPublicId = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                CloudinaryService.UploadResult result = cloudinaryService.uploadImage(photo, "riders-app/vehicles");
                photoUrl = result.url();
                photoPublicId = result.publicId();
            } catch (IOException e) {
                throw new BadRequestException("Errore durante il caricamento della foto");
            }
        }

        Vehicle vehicle = new Vehicle(
                currentUser, model, body.nickname(), body.year(),
                body.licensePlate(), body.vin(), body.color(), body.initialMileage(), photoUrl
        );
        vehicle.setPhotoPublicId(photoPublicId);

        vehicleRepository.save(vehicle);
        log.info("Utente {} ha aggiunto il veicolo {}", currentUser.getId(), vehicle.getId());
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleResponseDTO updateVehicle(User currentUser, UUID vehicleId, UpdateVehicleRequestDTO body) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));

        if (body.nickname() != null) vehicle.setNickname(body.nickname());
        if (body.year() != null) vehicle.setYear(body.year());
        if (body.vin() != null) vehicle.setVin(body.vin());
        if (body.color() != null) vehicle.setColor(body.color());

        if (body.licensePlate() != null && !body.licensePlate().equals(vehicle.getLicensePlate())) {
            if (vehicleRepository.existsByLicensePlate(body.licensePlate())) {
                throw new BadRequestException("Targa già registrata");
            }
            vehicle.setLicensePlate(body.licensePlate());
        }

        vehicleRepository.save(vehicle);
        return toDTO(vehicle);
    }

    @Transactional
    public void deleteVehicle(User currentUser, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Veicolo non trovato nel tuo garage"));

        boolean wasCurrentVehicle = currentUser.getCurrentVehicle() != null
                && currentUser.getCurrentVehicle().getId().equals(vehicleId);

        if (wasCurrentVehicle) {
            currentUser.setCurrentVehicle(null);
            userRepository.save(currentUser);
        }

        vehicleRepository.delete(vehicle);

        if (vehicle.getPhotoPublicId() != null) {
            try {
                cloudinaryService.deleteImage(vehicle.getPhotoPublicId());
            } catch (IOException e) {
                log.warn("Impossibile cancellare la foto del veicolo {} eliminato (utente {})",
                        vehicle.getPhotoPublicId(), currentUser.getId(), e);
            }
        }

        log.info("Utente {} ha eliminato il veicolo {}", currentUser.getId(), vehicleId);
    }

    public List<VehicleResponseDTO> getUserVehicles(User currentUser) {
        return vehicleRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }


    private VehicleResponseDTO toDTO(Vehicle vehicle) {
        return new VehicleResponseDTO(
                vehicle.getId(),
                toModelDTO(vehicle.getModel()),
                vehicle.getNickname(),
                vehicle.getYear(),
                vehicle.getLicensePlate(),
                vehicle.getVin(),
                vehicle.getColor(),
                vehicle.getInitialMileage(),
                vehicle.getCurrentMileage(),
                vehicle.getPhotoUrl(),
                vehicle.getCreatedAt()
        );
    }

    private MotorcycleModelResponseDTO toModelDTO(MotorcycleModel model) {
        return new MotorcycleModelResponseDTO(
                model.getId(),
                new BrandResponseDTO(model.getBrand().getId(), model.getBrand().getName(), model.getBrand().getLogoUrl()),
                model.getName(), model.getEngineCc(), model.getCategory(),
                model.getYearStart(), model.getYearEnd(), model.getHorsePower(), model.getWeightKg(),
                model.getImageUrl()
        );
    }
}
