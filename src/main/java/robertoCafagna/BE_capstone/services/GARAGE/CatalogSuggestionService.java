package robertoCafagna.BE_capstone.services.GARAGE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.GARAGE.CatalogSuggestionRequestDTO;
import robertoCafagna.BE_capstone.entities.CatalogSuggestion;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.repositories.GARAGE.CatalogSuggestionRepository;
import robertoCafagna.BE_capstone.services.MailService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSuggestionService {

    private static final int MAX_SUGGESTIONS_PER_USER_PER_DAY = 5;

    private final MailService mailService;
    private final CatalogSuggestionRepository catalogSuggestionRepository;
    private final Map<UUID, DailyCounter> countersByUser = new ConcurrentHashMap<>();
    @Value("${app.mail.admin}")
    private String adminAddress;

    public void submit(User currentUser, CatalogSuggestionRequestDTO body) {
        checkAndIncrementLimit(currentUser.getId());

        CatalogSuggestion suggestion = new CatalogSuggestion(
                currentUser, body.brandName(), body.modelName(), body.engineCc(),
                body.category(), body.yearStart(), body.yearEnd(),
                body.horsePower(), body.weightKg(), body.note()
        );
        catalogSuggestionRepository.save(suggestion);

        mailService.sendCatalogSuggestionEmail(
                adminAddress, currentUser.getUsername(), currentUser.getEmail(),
                body.brandName(), body.modelName(), body.engineCc(),
                body.category() != null ? body.category().name() : null,
                body.yearStart(), body.yearEnd(), body.horsePower(), body.weightKg(),
                body.note()
        );
        log.info("Utente {} ha segnalato un modello mancante: {} {}", currentUser.getId(), body.brandName(), body.modelName());
    }

    private void checkAndIncrementLimit(UUID userId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        DailyCounter counter = countersByUser.computeIfAbsent(userId, id -> new DailyCounter(startOfDay, 0));

        if (counter.since.isBefore(startOfDay)) {
            counter = new DailyCounter(startOfDay, 0);
        }

        if (counter.count >= MAX_SUGGESTIONS_PER_USER_PER_DAY) {
            throw new BadRequestException("Hai raggiunto il limite giornaliero di segnalazioni");
        }

        countersByUser.put(userId, new DailyCounter(counter.since, counter.count + 1));
    }

    private record DailyCounter(LocalDateTime since, int count) {
    }
}
