package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import robertoCafagna.BE_capstone.entities.UserProfile;
import robertoCafagna.BE_capstone.repositories.UserProfileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfile getProfile(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow();
    }
}
