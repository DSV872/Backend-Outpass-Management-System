package com.outpass.auth_service.service;

import java.time.Year;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.outpass.auth_service.config.JwtProperties;
import com.outpass.auth_service.dto.LoginDto;
import com.outpass.auth_service.dto.LoginResponseDto;
import com.outpass.auth_service.dto.RegisterDto;
import com.outpass.auth_service.dto.UpdateUserRequest;
import com.outpass.auth_service.dto.UserResponse;
import com.outpass.auth_service.dto.UserValidationResponse;
import com.outpass.auth_service.exception.InvalidCredentialsException;
import com.outpass.auth_service.exception.UserBusinessException;
import com.outpass.auth_service.exception.UserNotFoundException;
import com.outpass.auth_service.jwtservice.JwtService;
import com.outpass.auth_service.model.RoleType;
import com.outpass.auth_service.model.User;
import com.outpass.auth_service.repo.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepo;
	private final PasswordEncoder encoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	@Transactional
	public UserResponse createUser(RegisterDto register) {
		RoleType role = register.getRole();
		log.info("Creating user: role={}", role);
		if (role == RoleType.ADMIN) {
			log.warn("Attempt to create another ADMIN user");
			throw new UserBusinessException("Creating another ADMIN user is not allowed");
		}
		String userId = generateUserId(role);
		String email = register.getEmail();
		if (userRepo.existsByUserId(userId)) {
			log.error("Generated user ID already exists: userId={}", userId);
			throw new UserBusinessException("Generated User ID already exists: " + userId);
		}
		if (userRepo.existsByEmail(email)) {
			log.error("Generated email already exists: email={}", email);
			throw new UserBusinessException("Generated email already exists: " + email);
		}
		User user = new User();
		user.setUserId(userId);
		user.setEmail(email);
		user.setPassword(encoder.encode(register.getPassword()));
		user.setRole(role);
		user.setEnabled(true);
		User savedUser = userRepo.save(user);
		log.info("User created successfully: userId={}, role={}", savedUser.getUserId(), savedUser.getRole());
		return toUserResponse(savedUser);
	}

	private String generateUserId(RoleType role) {
		String prefix = switch (role) {
		case STUDENT -> "O";
		case WARDEN -> "WD";
		case SECURITY -> "SC";
		case ADMIN -> {
			log.warn("Attempt to generate user ID for ADMIN");
			throw new UserBusinessException("Creating another ADMIN user is not allowed");
		}
		};
		String year = String.valueOf(Year.now().getValue()).substring(2);
		String searchPrefix = prefix + year;
		List<String> userIds = userRepo.findLastUserIdStartingWith(searchPrefix, PageRequest.of(0, 1));
		int nextNumber = 1;
		if (!userIds.isEmpty()) {
			String lastUserId = userIds.get(0);
			String numberPart = lastUserId.substring(searchPrefix.length());
			nextNumber = Integer.parseInt(numberPart) + 1;
		}
		return searchPrefix + String.format("%03d", nextNumber);
	}

	public List<UserResponse> getAllUsers() {
		log.debug("Fetching all non-admin users");
		List<UserResponse> users = userRepo.findAllByRoleNot(RoleType.ADMIN).stream().map(this::toUserResponse)
				.toList();
		log.debug("Users retrieved: count={}", users.size());
		return users;
	}

	public LoginResponseDto login(@Valid LoginDto login) {
		log.info("Login attempt: email={}", login.getEmail());
		User user = userRepo.findByEmail(login.getEmail()).orElseThrow(() -> {
			log.warn("Login failed: invalid credentials, email={}", login.getEmail());
			return new InvalidCredentialsException("Invalid credentials");
		});
		if (!user.isEnabled()) {
			log.warn("Login failed: disabled account, userId={}", user.getUserId());
			throw new InvalidCredentialsException("Invalid credentials");
		}
		if (!encoder.matches(login.getPassword(), user.getPassword())) {
			log.warn("Login failed: invalid password, userId={}", user.getUserId());
			throw new InvalidCredentialsException("Invalid credentials");
		}
		String token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());
		log.info("Login successful: userId={}, role={}", user.getUserId(), user.getRole());
		return new LoginResponseDto(token, user.getRole().name(), jwtProperties.getExpiration());
	}

	public UserValidationResponse validateUser(String userId) {
		log.debug("Validating user: userId={}", userId);
		User user = userRepo.findByUserId(userId).orElseThrow(() -> {
			log.warn("User not found during validation: userId={}", userId);
			return new UserNotFoundException("User not found: " + userId);
		});
		return new UserValidationResponse(user.getUserId(), user.getEmail(), user.getRole().name(), user.isEnabled());
	}

	@Transactional
	public UserResponse updateUserStatus(String userId, Boolean enabled) {
		log.info("Updating user status: userId={}, enabled={}", userId, enabled);
		User user = userRepo.findByUserId(userId).orElseThrow(() -> {
			log.warn("User not found for status update: userId={}", userId);
			return new UserNotFoundException("User not found: " + userId);
		});
		if (user.getRole() == RoleType.ADMIN) {
			log.warn("Attempt to change ADMIN status: userId={}", userId);
			throw new UserBusinessException("ADMIN user status cannot be changed");
		}
		user.setEnabled(enabled);
		User savedUser = userRepo.save(user);
		log.info("User status updated: userId={}, enabled={}", savedUser.getUserId(), savedUser.isEnabled());
		return toUserResponse(savedUser);
	}

	@Transactional
	public UserResponse updateUser(String userId, UpdateUserRequest request) {
		log.info("Updating user: userId={}, newRole={}", userId, request.getRole());
		User user = userRepo.findByUserId(userId).orElseThrow(() -> {
			log.warn("User not found for update: userId={}", userId);
			return new UserNotFoundException("User not found: " + userId);
		});
		if (user.getRole() == RoleType.ADMIN) {
			log.warn("Attempt to modify ADMIN user: userId={}", userId);
			throw new UserBusinessException("ADMIN user cannot be modified");
		}
		user.setRole(request.getRole());
		User savedUser = userRepo.save(user);
		log.info("User updated successfully: userId={}, role={}", savedUser.getUserId(), savedUser.getRole());
		return toUserResponse(savedUser);
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getUserId(), user.getEmail(), user.getRole(), user.isEnabled());
	}
}