package robertoCafagna.BE_capstone.services.RIDE;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class MapboxRateLimiter {

    private final AtomicInteger callsToday = new AtomicInteger(0);
    @Value("${mapbox.directions.daily-limit:100}")
    private int dailyLimit;


    public void checkAndIncrement() {
        int current = callsToday.incrementAndGet();
        if (current > dailyLimit) {
            callsToday.decrementAndGet(); // non conto la chiamata rifiutata
            throw new BadRequestException(
                    "Limite giornaliero di calcoli percorso raggiunto. Riprova domani."
            );
        }
        log.info("Chiamata Mapbox Directions {}/{} oggi", current, dailyLimit);
    }

    @Scheduled(cron = "0 0 0 * * *") // mezzanotte ogni giorno
    public void resetCounter() {
        int previous = callsToday.getAndSet(0);
        log.info("Reset contatore Mapbox Directions — erano state fatte {} chiamate", previous);
    }
}
