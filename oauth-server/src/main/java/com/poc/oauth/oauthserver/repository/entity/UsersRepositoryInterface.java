package com.poc.oauth.oauthserver.repository.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepositoryInterface extends JpaRepository<UserEntity, String> {
	
		Optional<UserEntity> findByUsername(String username);

}
