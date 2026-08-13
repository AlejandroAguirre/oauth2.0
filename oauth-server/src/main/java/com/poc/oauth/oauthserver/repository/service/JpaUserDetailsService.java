package com.poc.oauth.oauthserver.repository.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poc.oauth.oauthserver.repository.entity.UserEntity;
import com.poc.oauth.oauthserver.repository.entity.UsersRepositoryInterface;

@Service
public class JpaUserDetailsService implements UserDetailsService {

	private UsersRepositoryInterface repository;

	public JpaUserDetailsService(UsersRepositoryInterface repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<UserEntity> optionalUser = repository.findByUsername(username);

		if (!optionalUser.isPresent()) {
			throw new UsernameNotFoundException(String.format("Username %s no existe", username));
		}

		UserEntity user = optionalUser.orElseThrow(null);
		List<String> roles = new ArrayList<>();
		roles.add(user.getRoleName());

		List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		return new org.springframework.security.core.userdetails.User(username, user.getPassword(), true, true, true,
				true, authorities);
	}

}
