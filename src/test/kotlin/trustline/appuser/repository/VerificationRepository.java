package trustline.appuser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import trustline.appuser.model.VerificationModel;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationModel, UUID> {
    Optional<VerificationModel> findByUserIdAndPin(UUID userId, String pin);
}
