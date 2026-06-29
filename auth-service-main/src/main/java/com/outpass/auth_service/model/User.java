package com.outpass.auth_service.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true, length = 100)
	private String email;
	
	@Column(nullable = false, length = 255)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable =false)
	private RoleType role;
	
	@Column(nullable = false)
	private boolean enabled;
	
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.enabled = true;
	}
	
	
}
