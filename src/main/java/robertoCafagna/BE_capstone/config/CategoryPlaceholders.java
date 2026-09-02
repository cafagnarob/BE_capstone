package robertoCafagna.BE_capstone.config;

import robertoCafagna.BE_capstone.enums.MotorcycleCategory;

import java.util.Map;

public class CategoryPlaceholders {

    private static final Map<MotorcycleCategory, String> IMAGES = Map.of(
            MotorcycleCategory.NAKED, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273597/naked_hb3rtm.png",
            MotorcycleCategory.ADVENTURE, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273564/adventure_ngaep0.png",
            MotorcycleCategory.SPORT_TOURING, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273546/sport_touring_pq3bsh.png",
            MotorcycleCategory.ENDURO, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273574/cross_yuequp.png",
            MotorcycleCategory.CRUISER, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273524/cruiser_lzpirc.png",
            MotorcycleCategory.SPORT, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273608/sport_af1voz.png",
            MotorcycleCategory.TOURING, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273643/touring_yxqtpp.png",
            MotorcycleCategory.CUSTOM, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273585/custom_ddmzet.png",
            MotorcycleCategory.SCOOTER, "https://res.cloudinary.com/ehgscudu/image/upload/v1788273889/scooter_oaao55.png"
    );

    private CategoryPlaceholders() {
    }

    public static String resolve(String imageUrl, MotorcycleCategory category) {
        return (imageUrl != null && !imageUrl.isBlank()) ? imageUrl : IMAGES.get(category);
    }
}