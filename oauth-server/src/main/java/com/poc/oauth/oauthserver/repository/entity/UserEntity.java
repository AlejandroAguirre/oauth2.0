package com.poc.oauth.oauthserver.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

@Entity
@Table(name = "CAT_USERS")
public class UserEntity {

	@Id
	@Column(nullable = false, name = "EMAIL",length = 30)
	private String username;
	
	@Column(name = "FIRST_NAME", length = 30)
	private String firstName;

	@Column(name = "LAST_NAME", length = 50)
	private String lastName;

	@Column(length = 100)
	private String password;

	@Column(name = "ROLE_NAME", length = 30)
	private String roleName;

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPassword() {
		return password;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

}
