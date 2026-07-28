package robertoCafagna.BE_capstone.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import robertoCafagna.BE_capstone.exceptions.BadRequestException;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("File vuoto");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
            throw new BadRequestException("Inserire solo immagini");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Immagine troppo grande. Massimo 5MB");
        }
        Map<?, ?> risultato = cloudinary
                .uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder
                        )
                );
        return risultato
                .get("secure_url")
                .toString();
    }

    public void deleteImage(String publicId)
            throws IOException {
        cloudinary.uploader()
                .destroy(publicId, ObjectUtils.emptyMap());
    }
}
