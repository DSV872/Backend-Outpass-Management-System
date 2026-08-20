package com.outpass.profile_service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.outpass.profile_service.model.SecurityProfile;

@Repository
public interface SecurityProfileRepository extends JpaRepository<SecurityProfile, Long> {

    Optional<SecurityProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}