package robertoCafagna.BE_capstone.services.SOCIAL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import robertoCafagna.BE_capstone.DTO.SOCIAL.NotificationResponseDTO;
import robertoCafagna.BE_capstone.DTO.SOCIAL.UnreadCountDTO;
import robertoCafagna.BE_capstone.entities.Event;
import robertoCafagna.BE_capstone.entities.Notification;
import robertoCafagna.BE_capstone.entities.Post;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.enums.NotificationType;
import robertoCafagna.BE_capstone.enums.ReferenceType;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.SOCIAL.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // --- lettura ---

    public Page<NotificationResponseDTO> getMyNotifications(User currentUser, int page, int size) {
        if (size <= 0 || size > 50) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size);

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(this::toDTO);
    }

    public UnreadCountDTO getUnreadCount(User currentUser) {
        return new UnreadCountDTO(notificationRepository.countByUserIdAndReadFalse(currentUser.getId()));
    }

    @Transactional
    public void markAsRead(User currentUser, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Notifica non trovata"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User currentUser) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(currentUser.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // --- generazione: un metodo per ogni evento di dominio ---

    @Transactional
    public void notifyNewFollower(User target, User follower) {
        create(target, follower, NotificationType.FOLLOW,
                follower.getUsername() + " ha iniziato a seguirti",
                follower.getId(), ReferenceType.USER);
    }

    @Transactional
    public void notifyNewLike(User postOwner, User liker, Post post) {
        if (postOwner.getId().equals(liker.getId())) return; // non ti notifichi da solo
        create(postOwner, liker, NotificationType.LIKE,
                liker.getUsername() + " ha messo like al tuo post",
                post.getId(), ReferenceType.POST);
    }

    @Transactional
    public void notifyNewComment(User postOwner, User commenter, Post post) {
        if (postOwner.getId().equals(commenter.getId())) return;
        create(postOwner, commenter, NotificationType.COMMENT,
                commenter.getUsername() + " ha commentato il tuo post",
                post.getId(), ReferenceType.POST);
    }

    @Transactional
    public void notifyEventInvite(User invitedUser, Event event) {
        create(invitedUser, event.getOrganizer(), NotificationType.EVENT_INVITE,
                "Sei stato invitato all'evento \"" + event.getTitle() + "\"",
                event.getId(), ReferenceType.EVENT);
    }

    @Transactional
    public void notifyParticipationRequest(User organizer, User requester, Event event) {
        create(organizer, requester, NotificationType.PARTICIPATION_REQUEST,
                requester.getUsername() + " ha richiesto di partecipare a \"" + event.getTitle() + "\"",
                event.getId(), ReferenceType.EVENT);
    }

    @Transactional
    public void notifyParticipationAccepted(User participant, Event event) {
        create(participant, event.getOrganizer(), NotificationType.PARTICIPATION_ACCEPTED,
                "La tua richiesta per \"" + event.getTitle() + "\" è stata accettata",
                event.getId(), ReferenceType.EVENT);
    }

    @Transactional
    public void notifyParticipationRejected(User participant, Event event) {
        create(participant, event.getOrganizer(), NotificationType.PARTICIPATION_REJECTED,
                "La tua richiesta per \"" + event.getTitle() + "\" è stata rifiutata",
                event.getId(), ReferenceType.EVENT);
    }

    private void create(User recipient, User actor, NotificationType type, String message, UUID referenceId, ReferenceType referenceType) {
        Notification notification = new Notification(recipient, actor, type, message, referenceId, referenceType);
        notificationRepository.save(notification);
    }

    private NotificationResponseDTO toDTO(Notification n) {
        return new NotificationResponseDTO(
                n.getId(), n.getType(), n.getMessage(), n.isRead(),
                n.getReferenceId(), n.getReferenceType(), n.getCreatedAt(),
                n.getActor() != null ? n.getActor().getUsername() : null,
                n.getActor() != null ? n.getActor().getProfilePicture() : null
        );
    }
}