package com.trustline.trustline.appuser.repository;

import com.trustline.trustline.appuser.model.VerificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationModel, UUID> {
}
