package com.outpass.profile_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.outpass.profile_service.config.FeignConfig;
import com.outpass.profile_service.dto.UserValidationResponse;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE",url = "${services.auth.url}", configuration = FeignConfig.class	)
public interface AuthServiceClient {

	@GetMapping("/internal/users/{userId}")
	UserValidationResponse getUser(@PathVariable("userId") String userId);
}