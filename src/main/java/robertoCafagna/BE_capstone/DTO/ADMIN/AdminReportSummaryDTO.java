package robertoCafagna.BE_capstone.DTO.ADMIN;

import robertoCafagna.BE_capstone.enums.ReportReason;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.enums.ReportTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminReportSummaryDTO(
        UUID id, String reporterUsername, ReportTargetType targetType, UUID targetId,
        ReportReason reason, String note, ReportStatus status, LocalDateTime createdAt,
        long pendingReportCountForTarget, String targetPreview, UUID linkedPostId
) {
}
