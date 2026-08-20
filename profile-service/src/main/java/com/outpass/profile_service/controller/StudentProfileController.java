package com.outpass.profile_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.outpass.profile_service.dto.AdminStudentProfileUpdateRequest;
import com.outpass.profile_service.dto.StudentProfileCreateRequest;
import com.outpass.profile_service.dto.StudentProfileResponse;
import com.outpass.profile_service.dto.StudentProfileUpdateRequest;
import com.outpass.profile_service.service.StudentProfileService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles/students")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProfileResponse> create(
            @Valid @RequestBody StudentProfileCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studentProfileService.create(request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<StudentProfileResponse> getByUserId(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                studentProfileService.getByUserId(userId)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentProfileResponse>> getAll() {

        return ResponseEntity.ok(
                studentProfileService.getAll()
        );
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> updateByStudent(
            @PathVariable String userId,
            @Valid @RequestBody StudentProfileUpdateRequest request) {

        return ResponseEntity.ok(
                studentProfileService.updateByStudent(userId, request)
        );
    }

    @PutMapping("/{userId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProfileResponse> updateByAdmin(
            @PathVariable String userId,
            @Valid @RequestBody AdminStudentProfileUpdateRequest request) {

        return ResponseEntity.ok(
                studentProfileService.updateByAdmin(userId, request)
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String userId) {

        studentProfileService.delete(userId);

        return ResponseEntity.noContent().build();
    }
}