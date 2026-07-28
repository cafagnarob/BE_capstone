package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.AdminUserSummaryDTO;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.UserRepository;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("username", "email", "createdAt", "lastLogin");
    private final UserRepository userRepository;

    public Page<AdminUserSummaryDTO> getAll(int page, int size, String orderBy) {
        if (size <= 0 || size > 20) size = 20;
        if (page < 0) page = 0;
        if (!ALLOWED_SORT_FIELDS.contains(orderBy)) {
            throw new BadRequestException(
                    "Campo di ordinamento non valido. Valori ammessi: " + ALLOWED_SORT_FIELDS
            );
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orderBy));
        return userRepository.findAll(pageable).map(this::toAdminSummary);
    }


    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        log.info("Admin ha disattivato l'utente {}", id);
    }

    public void reactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
        user.setActive(true);
        userRepository.save(user);
        log.info("Admin ha riattivato l'utente {}", id);
    }


    //mapper manuale
    private AdminUserSummaryDTO toAdminSummary(User user) {
        return new AdminUserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }
}
