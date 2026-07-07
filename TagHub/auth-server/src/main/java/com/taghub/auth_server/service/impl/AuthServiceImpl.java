package com.taghub.auth_server.service.impl;

import com.taghub.auth_server.MailServer;
import com.taghub.auth_server.dto.request.LoginRequest;
import com.taghub.auth_server.dto.request.RegisterRequest;
import com.taghub.auth_server.dto.response.AuthResponse;
import com.taghub.auth_server.entity.User;
import com.taghub.auth_server.repository.UserRepository;
import com.taghub.auth_server.service.AuthService;
import com.taghub.auth_server.service.JwtService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
	private final JwtService jwtService;
	private final MailServer mailServer;

	@Transactional
	@Override
	public User register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Bu e-posta zaten kullanımda!");
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());

		String encodedPassword = passwordEncoder.encode(request.getPassword());
		user.setPassword(encodedPassword);

		user.setCreatedAt(LocalDateTime.now());

		User savedUser = userRepository.save(user);
		logger.info("New user registered successfully: {}", savedUser.getEmail());

		return savedUser;
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı!"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("E-posta veya şifre hatalı!");
		}

		String token = jwtService.generateToken(user);

		logger.info("User logged in successfully!");
		return new AuthResponse(token, user.getEmail());
	}

	public void resetPasswordRequest(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Bu e-posta adresi ile kayıtlı kullanıcı bulunamadı!"));
		String resetCode = String.valueOf((int) (Math.random() * 900000) + 100000);
		user.setResetCode(resetCode);
		user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
		userRepository.save(user);
		mailServer.sendPasswordResetMail(email, resetCode);
	}

	public void verifyAndChangePassword(String email, String code, String newPassword) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
		if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
			throw new RuntimeException("Geçersiz şifre sıfırlama kodu!");
		}
		if (user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("Kodun süresi dolmuş! Lütfen yeniden kod talep edin.");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetCode(null);
		user.setResetCodeExpiresAt(null);
		userRepository.save(user);
	}


}

