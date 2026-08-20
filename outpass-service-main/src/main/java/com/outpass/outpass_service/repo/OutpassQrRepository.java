package com.outpass.outpass_service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.outpass.outpass_service.model.OutpassQr;

public interface OutpassQrRepository extends JpaRepository<OutpassQr, Long> {

	Optional<OutpassQr> findByOutpassId(Long outpassId);
}