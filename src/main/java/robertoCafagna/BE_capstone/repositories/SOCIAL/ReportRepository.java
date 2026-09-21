package robertoCafagna.BE_capstone.repositories.SOCIAL;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.Report;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.enums.ReportTargetType;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(UUID reporterId, ReportTargetType targetType, UUID targetId);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    long countByTargetTypeAndTargetIdAndStatus(ReportTargetType targetType, UUID targetId, ReportStatus status);

    void deleteByTargetTypeAndTargetId(ReportTargetType targetType, UUID targetId);

    Page<Report> findByTargetType(ReportTargetType targetType, Pageable pageable);

    Page<Report> findByStatusAndTargetType(ReportStatus status, ReportTargetType targetType, Pageable pageable);

    List<Report> findByTargetTypeAndTargetIdAndStatus(ReportTargetType targetType, UUID targetId, ReportStatus status);

    long countByStatus(ReportStatus status);
}
