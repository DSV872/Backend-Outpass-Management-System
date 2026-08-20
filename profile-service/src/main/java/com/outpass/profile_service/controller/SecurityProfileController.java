package com.outpass.profile_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.outpass.profile_service.dto.SecurityProfileCreateRequest;
import com.outpass.profile_service.dto.SecurityProfileResponse;
import com.outpass.profile_service.dto.SecurityProfileUpdateRequest;
import com.outpass.profile_service.service.SecurityProfileService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles/security")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SecurityProfileController {

    private final SecurityProfileService securityProfileService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityProfileResponse> create(
            @Valid @RequestBody SecurityProfileCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(securityProfileService.create(request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SECURITY', 'ADMIN')")
    public ResponseEntity<SecurityProfileResponse> getByUserId(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                securityProfileService.getByUserId(userId)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityProfileResponse>> getAll() {

        return ResponseEntity.ok(
                securityProfileService.getAll()
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('SECURITY', 'ADMIN')")
    public ResponseEntity<SecurityProfileResponse> update(
            @PathVariable String userId,
            @Valid @RequestBody SecurityProfileUpdateRequest request) {

        return ResponseEntity.ok(
                securityProfileService.update(userId, request)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String userId) {

        securityProfileService.delete(userId);

        return ResponseEntity.noContent().build();
    }
}