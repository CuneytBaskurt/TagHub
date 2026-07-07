package com.taghub.auth_server.service;

import com.taghub.auth_server.dto.request.LoginRequest;
import com.taghub.auth_server.dto.request.RegisterRequest;
import com.taghub.auth_server.dto.response.AuthResponse;
import com.taghub.auth_server.entity.User;
import com.taghub.auth_server.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthService authService;

	@Test
	public void AuthService_RegisterUser_ReturnsSavedUser(){

		RegisterRequest registerRequest = RegisterRequest.builder()
				.username("cuneyt")
				.email("cuneyt@gmail.com")
				.password("123456")
				.build();

		User user = User.builder()
				.username("cuneyt")
				.email("cuneyt@gmail.com")
				.password("encoded_123456")
				.build();

		when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(false);

		when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encoded_123456");

		when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

		User savedUser = authService.register(registerRequest);

		Assertions.assertNotNull(savedUser);
		Assertions.assertEquals("cuneyt@gmail.com", savedUser.getUsername());
		Assertions.assertEquals("cuneyt@gmail.com", savedUser.getEmail());
		Assertions.assertEquals("encoded_123456", savedUser.getPassword());
	}

	@Test
	public void AuthService_LoginUser_ReturnTrueUser(){
		LoginRequest loginRequest = LoginRequest.builder()
				.email("test@gmail.com")
				.password("raw_password")
				.build();

		User user = User.builder()
				.email("test@gmail.com")
				.password("encoded_password")
				.build();

		Mockito.when(userRepository.findByEmail(loginRequest.getEmail()))
				.thenReturn(Optional.of(user));

		Mockito.when(passwordEncoder.matches("raw_password", "encoded_password"))
				.thenReturn(true);

		Mockito.when(jwtService.generateToken(user))
				.thenReturn("mock_jwt_token_123");

		AuthResponse response = authService.login(loginRequest);

		Assertions.assertNotNull(response);

		Assertions.assertEquals("mock_jwt_token_123", response.getToken());


	}

	@Test
	public void AuthService_RegisterByExistingEmail_ThrowException(){
		RegisterRequest registerRequest = RegisterRequest.builder()
				.username("cuneyt")
				.email("test@gmail.com")
				.password("raw_password")
				.build();

		Mockito.when(userRepository.existsByEmail(registerRequest.getEmail()))
				.thenReturn(true);

		RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
			authService.register(registerRequest);
		});

		Assertions.assertEquals("Bu e-posta zaten kullanımda!", exception.getMessage());

		Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
	}

	@Test
	public void AuthService_LoginWithNonExistingEmail_ThrowsException(){
		LoginRequest loginRequest = LoginRequest.builder()
				.email("test@gmail.com")
				.password("raw_password")
				.build();

		Mockito.when(userRepository.findByEmail(loginRequest.getEmail()))
				.thenReturn(Optional.empty());

		RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
			authService.login(loginRequest);
		});

		Assertions.assertEquals("E-posta veya şifre hatalı!", exception.getMessage());

		Mockito.verify(passwordEncoder, Mockito.never()).matches(Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void AuthService_LoginWithWrongPassword_ThrowsException(){
		LoginRequest loginRequest = LoginRequest.builder()
				.email("test@gmail.com")
				.password("raw_password")
				.build();

		User user = User.builder()
				.email("test@gmail.com")
				.password("encoded_password")
				.build();

		Mockito.when(userRepository.findByEmail(loginRequest.getEmail()))
				.thenReturn(Optional.of(user));

		Mockito.when(passwordEncoder.matches("raw_password", "encoded_password"))
				.thenReturn(false);

		RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
			authService.login(loginRequest);
		});

		Assertions.assertEquals("E-posta veya şifre hatalı!", exception.getMessage());

		Mockito.verify(jwtService, Mockito.never()).generateToken(Mockito.any(User.class));

		Mockito.verify(passwordEncoder, Mockito.times(1)).matches("raw_password", "encoded_password");
	}


}
