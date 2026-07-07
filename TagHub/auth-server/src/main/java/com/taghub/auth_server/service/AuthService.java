package com.taghub.auth_server.service;

import com.taghub.auth_server.dto.request.LoginRequest;
import com.taghub.auth_server.dto.response.AuthResponse;
import com.taghub.auth_server.entity.User;
import com.taghub.auth_server.repository.UserRepository;
import com.taghub.auth_server.dto.request.RegisterRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


public interface AuthService {
	User register(RegisterRequest request);
	AuthResponse login(LoginRequest request);
	void resetPasswordRequest(String email);
	void verifyAndChangePassword(String email, String code, String newPassword);
}
