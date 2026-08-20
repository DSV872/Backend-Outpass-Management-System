package com.outpass.profile_service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.outpass.profile_service.model.WardenProfile;

@Repository
public interface WardenProfileRepository extends JpaRepository<WardenProfile, Long> {

    Optional<WardenProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);
}