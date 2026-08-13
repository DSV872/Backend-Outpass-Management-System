package com.outpass.outpass_service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.outpass.outpass_service.model.EmailRecipient;
import com.outpass.outpass_service.model.OutpassEmailResend;

public interface OutpassEmailResendRepository
        extends JpaRepository<OutpassEmailResend, Long> {

    Optional<OutpassEmailResend> findByOutpass_IdAndRecipient(
            Long outpassId,
            EmailRecipient recipient);
}