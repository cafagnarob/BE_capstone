package robertoCafagna.BE_capstone.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.entities.User;
import robertoCafagna.BE_capstone.exceptions.NotFoundException;
import robertoCafagna.BE_capstone.repositories.UserRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("l'utente" + username + "non è stato trovato"));
    }

    public Page<User> getAll(int page, int size, String orderBy) {
        if (size > 20) size = 20;
        if (size < 0) size = 10;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(orderBy));
        return this.userRepository.findAll(pageable);
    }

    public void delete(UUID id) {
        User indFromDB = this.findById(id);
        this.userRepository.delete(indFromDB);
        log.info(
                "Utente {} eliminato",
                id
        );
    }

    public User updateProfilePicture(UUID userId, MultipartFile file) {
        User user = findById(userId);
        try {
            String imageUrl = cloudinaryService.uploadImage(file, "riders-app/users/profile");

            user.setProfilePicture(imageUrl);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore durante il caricamento immagine");
        }
    }
}
