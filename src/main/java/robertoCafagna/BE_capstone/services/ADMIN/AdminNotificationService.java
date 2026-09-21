package robertoCafagna.BE_capstone.services.ADMIN;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.repositories.USER.UserRepository;
import robertoCafagna.BE_capstone.services.SOCIAL.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void broadcastSystemMessage(String message) {
        List<User> activeUsers = userRepository.findByActiveTrue();
        notificationService.notifyBroadcast(activeUsers, message);
        log.info("Admin ha inviato una notifica broadcast a {} utenti attivi", activeUsers.size());
    }
}