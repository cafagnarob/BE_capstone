package robertoCafagna.BE_capstone.repositories.USER;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import robertoCafagna.BE_capstone.entities.ProfileLink;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileLinkRepository extends JpaRepository<ProfileLink, UUID> {
    List<ProfileLink> findByProfileId(UUID profileId);
}
