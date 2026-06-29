package com.outpass.auth_service.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.outpass.auth_service.config.JwtProperties;
import com.outpass.auth_service.dto.LoginDto;
import com.outpass.auth_service.dto.LoginResponseDto;
import com.outpass.auth_service.dto.RegisterDto;
import com.outpass.auth_service.jwtservice.jwtService;
import com.outpass.auth_service.model.RoleType;
import com.outpass.auth_service.model.User;
import com.outpass.auth_service.repo.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepo;
	private final PasswordEncoder encoder;
	private final jwtService jwtService;
	private final JwtProperties jwtProperties;
	
	public void register(RegisterDto register) {
		if(userRepo.findByEmail(register.getEmail()).isPresent()) {
			throw new RuntimeException("Email already exists!");
		}
		User user = new User();
		user.setEmail(register.getEmail());
		user.setPassword(encoder.encode(register.getPassword()));
		user.setEnabled(true);
		user.setRole(RoleType.STUDENT);
		user.setCreatedAt(LocalDateTime.now());
		userRepo.save(user);
	}

	public LoginResponseDto login(@Valid LoginDto login) {
		System.out.println(login);
		Optional<User> userDb = userRepo.findByEmail(login.getEmail());
		if(userDb.isEmpty()) {
			System.out.println("No user found");
			throw new RuntimeException("Invalid credentials");
		}
		User user = userDb.get();
		if(!encoder.matches(login.getPassword(), user.getPassword())) {
			throw new RuntimeException("invalid credentials");
		}
		String token = jwtService.generateToken(user.getEmail(),user.getRole().name());
		
		return new LoginResponseDto(token, user.getRole().toString(), jwtProperties.getExpiration());
	}
	
	

}
