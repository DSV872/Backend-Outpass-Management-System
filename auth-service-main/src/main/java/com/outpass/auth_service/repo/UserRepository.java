package com.outpass.auth_service.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.outpass.auth_service.model.RoleType;
import com.outpass.auth_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUserId(String userId);

	Optional<User> findByEmail(String email);

	boolean existsByUserId(String userId);

	boolean existsByEmail(String email);

	List<User> findAllByRoleNot(RoleType role);

	@Query("""
			SELECT u.userId
			FROM User u
			WHERE u.userId LIKE CONCAT(:prefix, '%')
			ORDER BY u.userId DESC
			""")
	List<String> findLastUserIdStartingWith(@Param("prefix") String prefix, Pageable pageable);
}