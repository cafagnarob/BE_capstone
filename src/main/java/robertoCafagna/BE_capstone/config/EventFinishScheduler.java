package robertoCafagna.BE_capstone.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.enums.EventStatus;
import robertoCafagna.BE_capstone.repositories.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventFinishScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 */15 * * * *") // ogni 15 minuti
    @Transactional
    public void finishExpiredEvents() {
        List<Event> expired = eventRepository.findByStatusAndEndDateTimeBefore(
                EventStatus.ACTIVE, LocalDateTime.now()
        );

        if (expired.isEmpty()) return;

        expired.forEach(e -> e.setStatus(EventStatus.FINISHED));
        eventRepository.saveAll(expired);
        log.info("Job schedulato: {} eventi passati automaticamente a FINISHED", expired.size());
    }
}

