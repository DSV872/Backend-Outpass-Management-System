package com.outpass.outpass_service.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.outpass.outpass_service.dto.ErrorResponseDto;
import com.outpass.outpass_service.exception.EmailResendException;
import com.outpass.outpass_service.exception.OutpassBusinessException;
import com.outpass.outpass_service.exception.OutpassNotFoundException;
import com.outpass.outpass_service.exception.UnauthorizedOutpassException;
import com.outpass.outpass_service.exception.UserValidationException;
import com.outpass.outpass_service.exception.WardenDutyException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		String message = ex.getBindingResult().getFieldError().getDefaultMessage();

		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION ERROR", message, request);
	}

	@ExceptionHandler(OutpassNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleOutpassNotFound(OutpassNotFoundException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.NOT_FOUND, "NOT FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(UnauthorizedOutpassException.class)
	public ResponseEntity<ErrorResponseDto> handleUnauthorized(UnauthorizedOutpassException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
	}

	@ExceptionHandler(UserValidationException.class)
	public ResponseEntity<ErrorResponseDto> handleUserValidation(UserValidationException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "USER VALIDATION ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(OutpassBusinessException.class)
	public ResponseEntity<ErrorResponseDto> handleBusiness(OutpassBusinessException ex, HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "BUSINESS ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(WardenDutyException.class)
	public ResponseEntity<ErrorResponseDto> handleWardenDuty(WardenDutyException ex, HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "WARDEN DUTY ERROR", ex.getMessage(), request);
	}

	@ExceptionHandler(EmailResendException.class)
	public ResponseEntity<ErrorResponseDto> handleEmailResend(EmailResendException ex, HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "EMAIL RESEND ERROR", ex.getMessage(), request);
	}

	private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String error, String message,
			HttpServletRequest request) {

		return ResponseEntity.status(status).body(
				new ErrorResponseDto(status.value(), error, message, request.getRequestURI(), LocalDateTime.now()));
	}
}