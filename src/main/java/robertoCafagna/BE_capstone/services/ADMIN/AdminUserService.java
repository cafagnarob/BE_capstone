package robertoCafagna.BE_capstone.services.ADMIN;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminUserSummaryDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.MailService;
import robertoCafagna.BE_capstone.services.UserService;
import robertoCafagna.BE_capstone.specifications.UserSpecifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("username", "email", "createdAt", "lastLogin");
    private final UserRepository userRepository;
    private final UserService userService;
    private final MailService mailService;

    public Page<AdminUserSummaryDTO> getAll(String query, String status, int page, int size, String orderBy) {
        if (size <= 0 || size > 20) size = 20;
        if (page < 0) page = 0;
        if (!ALLOWED_SORT_FIELDS.contains(orderBy)) {
            throw new BadRequestException(
                    "Campo di ordinamento non valido. Valori ammessi: " + ALLOWED_SORT_FIELDS
            );
        }

        List<Specification<User>> specs = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            specs.add(UserSpecifications.usernameOrEmailContains(query.trim()));
        }
        if ("ACTIVE".equals(status)) {
            specs.add(UserSpecifications.isActiveOnly());
        } else if ("DEACTIVATED".equals(status)) {
            specs.add(UserSpecifications.isDeactivatedOnly());
        } else if ("DELETED".equals(status)) {
            specs.add(UserSpecifications.isDeleted());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orderBy));
        return userRepository.findAll(Specification.allOf(specs), pageable).map(this::toAdminSummary);
    }


    public void deactivateUser(UUID id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        mailService.sendAccountDeactivatedByAdminEmail(user.getEmail(), user.getUsername(), reason);
        log.info("Admin ha disattivato l'utente {} (motivo: {})", id, reason);
    }

    public void reactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
        user.setActive(true);
        userRepository.save(user);
        mailService.sendAccountReactivatedByAdminEmail(user.getEmail(), user.getUsername());
        log.info("Admin ha riattivato l'utente {}", id);
    }

    public void deleteUser(UUID userId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Specificare un motivo per l'eliminazione forzata dell'account");
        }
        userService.adminDeleteAccount(userId, reason);
    }


    //mapper manuale
    private AdminUserSummaryDTO toAdminSummary(User user) {
        return new AdminUserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt(),
                user.getLastLogin(),
                user.getDeletedAt() != null
        );
    }
}
