package robertoCafagna.BE_capstone.services.ADMIN;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminEventSearchFilterDTO;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminEventSummaryDTO;
import robertoCafagna.BE_capstone.services.EVENT.EventService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminEventService {
    private final EventService eventService;

    public void cancelEvent(UUID eventId, String reason) {
        eventService.adminCancelEvent(eventId, reason);
    }

    public Page<AdminEventSummaryDTO> getAll(AdminEventSearchFilterDTO filters, int page, int size) {
        return eventService.adminSearchEvents(filters, page, size);
    }
}
