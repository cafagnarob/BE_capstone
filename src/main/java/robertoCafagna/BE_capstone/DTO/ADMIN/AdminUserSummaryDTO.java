package robertoCafagna.BE_capstone.DTO.ADMIN;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserSummaryDTO(UUID id,
                                  String username,
                                  String email,
                                  boolean active,
                                  LocalDateTime createdAt,
                                  LocalDateTime lastLogin) {
}
