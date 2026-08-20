package com.outpass.outpass_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.outpass.outpass_service.config.FeignConfig;
import com.outpass.outpass_service.dto.UserValidationResponse;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthServiceClient {

	@GetMapping("/internal/users/{userId}")
	UserValidationResponse getUser(@PathVariable("userId") String userId);
}