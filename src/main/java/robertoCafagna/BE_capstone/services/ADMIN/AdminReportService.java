package robertoCafagna.BE_capstone.services.ADMIN;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.ADMIN.AdminReportSummaryDTO;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.PostComment;
import robertoCafagna.BE_capstone.entities.Report;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.ReportStatus;
import robertoCafagna.BE_capstone.enums.ReportTargetType;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostCommentRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.ReportRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.SOCIAL.PostCommentService;
import robertoCafagna.BE_capstone.services.SOCIAL.PostService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public Page<AdminReportSummaryDTO> getAll(ReportStatus status, ReportTargetType targetType, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Report> reports;
        if (status != null && targetType != null) {
            reports = reportRepository.findByStatusAndTargetType(status, targetType, pageable);
        } else if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else if (targetType != null) {
            reports = reportRepository.findByTargetType(targetType, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        return reports.map(this::toDTO);
    }

    public void dismissReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        List<Report> pending = reportRepository.findByTargetTypeAndTargetIdAndStatus(
                report.getTargetType(), report.getTargetId(), ReportStatus.PENDING
        );
        pending.forEach(r -> r.setStatus(ReportStatus.DISMISSED));
        reportRepository.saveAll(pending);
        log.info("Admin ha respinto {} segnalazioni per {} {}", pending.size(), report.getTargetType(), report.getTargetId());
    }

    public void resolveReport(UUID reportId, String reason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));

        switch (report.getTargetType()) {
            case POST -> postService.adminDeletePost(report.getTargetId(), reason);
            case COMMENT -> postCommentService.adminDeleteComment(report.getTargetId(), reason);
            case USER ->
                    throw new BadRequestException("Per un profilo segnalato, gestisci l'account dalla pagina Utenti");
        }
    }

    private AdminReportSummaryDTO toDTO(Report r) {
        long pendingCount = reportRepository.countByTargetTypeAndTargetIdAndStatus(
                r.getTargetType(), r.getTargetId(), ReportStatus.PENDING
        );
        TargetInfo info = resolveTargetInfo(r.getTargetType(), r.getTargetId());
        return new AdminReportSummaryDTO(
                r.getId(), r.getReporter().getUsername(), r.getTargetType(), r.getTargetId(),
                r.getReason(), r.getNote(), r.getStatus(), r.getCreatedAt(),
                pendingCount, info.preview(), info.linkedPostId()
        );
    }

    private TargetInfo resolveTargetInfo(ReportTargetType type, UUID targetId) {
        return switch (type) {
            case POST -> {
                Optional<Post> post = postRepository.findById(targetId);
                yield new TargetInfo(post.map(Post::getText).orElse("(post rimosso)"), post.map(Post::getId).orElse(null));
            }
            case COMMENT -> {
                Optional<PostComment> comment = postCommentRepository.findById(targetId);
                yield new TargetInfo(
                        comment.map(PostComment::getText).orElse("(commento rimosso)"),
                        comment.map(c -> c.getPost().getId()).orElse(null)
                );
            }
            case USER ->
                    new TargetInfo(userRepository.findById(targetId).map(User::getUsername).orElse("(utente rimosso)"), null);
        };
    }

    private record TargetInfo(String preview, UUID linkedPostId) {
    }
}
