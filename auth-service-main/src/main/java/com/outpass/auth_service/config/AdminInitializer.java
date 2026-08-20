package com.outpass.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.outpass.auth_service.model.RoleType;
import com.outpass.auth_service.model.User;
import com.outpass.auth_service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	@Value("${admin.user-id}")
	private String adminUserId;
	@Value("${admin.email}")
	private String adminEmail;
	@Value("${admin.password}")
	private String adminPassword;

	@Override
	public void run(String... args) {
		log.info("Checking admin user initialization");
		if (userRepository.existsByUserId(adminUserId) || userRepository.existsByEmail(adminEmail)) {
			log.info("Admin user already exists. Skipping admin creation");
			return;
		}
		User admin = new User();
		admin.setUserId(adminUserId);
		admin.setEmail(adminEmail);
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(RoleType.ADMIN);
		admin.setEnabled(true);
		userRepository.save(admin);
		log.info("Admin user created successfully: userId={}, role={}", adminUserId, RoleType.ADMIN);
	}
}