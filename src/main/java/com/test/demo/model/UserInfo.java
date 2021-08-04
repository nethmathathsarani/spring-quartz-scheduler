package com.test.demo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;

@Entity
@Table(name = "users")
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;



	@Id
	@Column(name = "username", length = 50)
	private String userName;

	@Column(name = "password", length = 800)
	private String password;

	@Column(name = "role", length = 50)
	private String role;

	@Column(name = "enabled")
	private short enabled;

	@Column(name = "email")
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}




	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public short getEnabled() {
		return enabled;
	}

	public void setEnabled(short enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return String.format("UserInfo [userName=%s, password=%s, role=%s, enabled=%s]", userName, password,
				role, enabled);
	}

}
