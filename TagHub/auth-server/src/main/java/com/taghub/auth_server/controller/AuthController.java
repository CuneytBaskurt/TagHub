package com.taghub.auth_server.controller;

import com.taghub.auth_server.dto.request.LoginRequest;
import com.taghub.auth_server.dto.request.RegisterRequest;
import com.taghub.auth_server.dto.response.AuthResponse;
import com.taghub.auth_server.entity.User;
import com.taghub.auth_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	// 🎯 1. KISIM: ŞİFRE SIFIRLAMA KODU İSTEME KAPISI
	@PostMapping("/reset-password-request")
	public ResponseEntity<String> requestResetPassword(@RequestBody ResetRequest request) {
		// Kendi servis sınıfının adı neyse onunla çağır abi (örn: userService veya authService)
		authService.resetPasswordRequest(request.email());

		return ResponseEntity.ok("Şifre sıfırlama kodu başarıyla e-posta adresinize gönderildi, abi.");
	}

	// 🎯 2. KISIM: KODU DOĞRULAMA VE ŞİFREYİ DEĞİŞTİRME KAPISI
	@PostMapping("/verify-reset-code")
	public ResponseEntity<String> verifyAndChangePassword(@RequestBody PasswordVerifyRequest request) {
		// Servis katmanındaki o ikinci doğrulama metodunu tetikliyoruz
		authService.verifyAndChangePassword(request.email(), request.code(), request.newPassword());

		return ResponseEntity.ok("Şifreniz başarıyla güncellendi! Yeni şifrenizle giriş yapabilirsiniz abi.");
	}


	public record ResetRequest(String email) {}

	public record PasswordVerifyRequest(String email, String code, String newPassword) {}
}

