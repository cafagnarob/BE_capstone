package robertoCafagna.BE_capstone.services.SOCIAL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.DTO.SOCIAL.CreateReportRequestDTO;
import robertoCafagna.BE_capstone.entities.PostComment;
import robertoCafagna.BE_capstone.entities.Report;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.ReportTargetType;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostCommentRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.PostRepository;
import robertoCafagna.BE_capstone.repositories.SOCIAL.ReportRepository;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;

    public void reportPost(User currentUser, UUID postId, CreateReportRequestDTO body) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post non trovato");
        }
        submit(currentUser, ReportTargetType.POST, postId, body);
    }

    public void reportComment(User currentUser, UUID commentId, CreateReportRequestDTO body) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Commento non trovato"));
        submit(currentUser, ReportTargetType.COMMENT, commentId, body);
    }

    public void reportUser(User currentUser, String username, CreateReportRequestDTO body) {
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (target.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Non puoi segnalare te stesso");
        }
        submit(currentUser, ReportTargetType.USER, target.getId(), body);
    }

    private void submit(User currentUser, ReportTargetType targetType, UUID targetId, CreateReportRequestDTO body) {
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(currentUser.getId(), targetType, targetId)) {
            throw new BadRequestException("Hai già segnalato questo contenuto");
        }

        Report report = new Report(currentUser, targetType, targetId, body.reason(), body.note());
        reportRepository.save(report);
        log.info("Utente {} ha segnalato {} {} (motivo: {})", currentUser.getId(), targetType, targetId, body.reason());
    }
}
