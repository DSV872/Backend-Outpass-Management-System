package com.outpass.outpass_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.outpass.outpass_service.dto.StudentParentResponse;
import com.outpass.outpass_service.dto.WardenDutyResponse;

@FeignClient(name = "profile-service")
public interface ProfileServiceClient {

	@GetMapping("/internal/students/{userId}/parent")
	StudentParentResponse getStudentParent(@PathVariable("userId") String userId);

	@GetMapping("/internal/warden-duties/today")
	List<WardenDutyResponse> getTodaysWardenDuties();
}