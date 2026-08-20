package com.outpass.profile_service.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.outpass.profile_service.dto.ErrorResponseDto;
import com.outpass.profile_service.exception.ProfileBusinessException;
import com.outpass.profile_service.exception.SecurityProfileNotFoundException;
import com.outpass.profile_service.exception.StudentProfileNotFoundException;
import com.outpass.profile_service.exception.UserValidationException;
import com.outpass.profile_service.exception.WardenDutyException;
import com.outpass.profile_service.exception.WardenDutyNotFoundException;
import com.outpass.profile_service.exception.WardenProfileNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(SecurityProfileNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleSecurityProfileNotFound(SecurityProfileNotFoundException ex,
			HttpServletRequest request) {
		log.warn("Security profile not found: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, "SECURITY PROFILE NOT FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(StudentProfileNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleStudentProfileNotFound(StudentProfileNotFoundException ex,
			HttpServletRequest request) {
		log.warn("Student profile not found: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, "STUDENT PROFILE NOT FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(WardenProfileNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleWardenProfileNotFound(WardenProfileNotFoundException ex,
			HttpServletRequest request) {
		log.warn("Warden profile not found: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, "WARDEN PROFILE NOT FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(WardenDutyNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleWardenDutyNotFound(WardenDutyNotFoundException ex,
			HttpServletRequest request) {
		log.warn("Warden duty not found: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.NOT_FOUND, "WARDEN DUTY NOT FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(UserValidationException.class)
	public ResponseEntity<ErrorResponseDto> handleUserValidation(UserValidationException ex,
			HttpServletRequest request) {
		log.warn("User validation failed: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "USER VALIDATION ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(WardenDutyException.class)
	public ResponseEntity<ErrorResponseDto> handleWardenDutyException(WardenDutyException ex,
			HttpServletRequest request) {
		log.warn("Warden duty business error: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "WARDEN DUTY ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(ProfileBusinessException.class)
	public ResponseEntity<ErrorResponseDto> handleProfileBusinessException(ProfileBusinessException ex,
			HttpServletRequest request) {
		log.warn("Profile business error: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "PROFILE BUSINESS ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getDefaultMessage()).orElse("Invalid request");
		log.warn("Request validation failed: path={}, message={}", request.getRequestURI(), message);
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION ERROR", message, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {
		log.warn("Constraint validation failed: path={}, message={}", request.getRequestURI(), ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponseDto> handleMethodValidation(HandlerMethodValidationException ex,
			HttpServletRequest request) {
		log.warn("Method validation failed: path={}", request.getRequestURI());
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION ERROR", "Invalid request parameters", request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponseDto> handleUnreadableRequest(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		log.warn("Malformed request body: path={}", request.getRequestURI());
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID REQUEST", "Request body is invalid or malformed",
				request);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException ex,
			HttpServletRequest request) {
		log.warn("Missing request parameter: path={}, parameter={}", request.getRequestURI(), ex.getParameterName());
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID REQUEST",
				"Required parameter is missing: " + ex.getParameterName(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception ex, HttpServletRequest request) {
		log.error("Unexpected error: path={}", request.getRequestURI(), ex);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR", "An unexpected error occurred",
				request);
	}

	private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String error, String message,
			HttpServletRequest request) {
		ErrorResponseDto response = new ErrorResponseDto(status.value(), error, message, request.getRequestURI(),
				LocalDateTime.now());
		return ResponseEntity.status(status).body(response);
	}
} 