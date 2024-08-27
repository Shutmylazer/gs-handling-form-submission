package com.example.securelogin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class User {

	@NotNull
	@Size(min=2, max=50)
	private String username;

	@NotNull
	@Size(min=2, max=200)
	private String password;

	@Size(max=255)
	private String salt;

	@Size(min=2, max=50)
	private String name;

	@Size(min=2, max=50)
	private String surname;

	@Size(min=2, max=50)
	private String telephone;

	public User(String username, String password, String salt, String name, String surname) {
		this.username = username;
		this.password = password;
		this.salt = salt;
		this.name = name;
		this.surname = surname;
	}

	public User(String username, String password, String salt, String name, String surname, String telephone) {
		this.username = username;
		this.password = password;
		this.salt = salt;
		this.name = name;
		this.surname = surname;
		this.telephone = telephone;
	}

	public User() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Override
	public String toString() {
		return "User{" +
				"username='" + username + '\'' +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", telephone='" + telephone + '\'' +
				'}';
	}
}
