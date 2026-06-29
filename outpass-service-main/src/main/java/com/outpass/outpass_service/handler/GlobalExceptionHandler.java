package com.outpass.outpass_service.handler;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.outpass.outpass_service.dto.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
		String message = ex.getBindingResult()
				.getFieldError()
				.getDefaultMessage();
		return ResponseEntity.badRequest().body(
				new ErrorResponseDto(400,"VALIDATION ERROR",message,request.getRequestURI(),LocalDateTime.now()));
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponseDto> handleRuntime(RuntimeException ex, HttpServletRequest request){
		return ResponseEntity.badRequest().body(new ErrorResponseDto(400,"BUSINESS ERROR",ex.getMessage(),request.getRequestURI(),LocalDateTime.now()));
	}
	
	
}
