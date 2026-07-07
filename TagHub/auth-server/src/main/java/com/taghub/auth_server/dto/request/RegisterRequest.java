package com.taghub.auth_server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegisterRequest {
	@NotBlank(message = "Kullanıcı adı boş olamaz!")
	@Size(min = 3, max = 20, message = "Kullanıcı adı 3-20 karakter arasında olmalı!")
	private String username;

	@NotBlank(message = "Email boş olamaz!")
	@Email(message = "Geçerli bir email adresi giriniz!")
	private String email;

	@NotBlank(message = "Şifre boş olamaz!")
	@Size(min = 6, message = "Şifre en az 6 karakter olmalı!")
	private String password;
}
