package com.taghub.auth_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taghub.auth_server.dto.request.LoginRequest;
import com.taghub.auth_server.dto.request.RegisterRequest;
import com.taghub.auth_server.dto.response.AuthResponse;
import com.taghub.auth_server.entity.User;
import com.taghub.auth_server.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void AuthController_RegisterUser_ReturnRegistered() throws Exception {

		RegisterRequest registerRequest = RegisterRequest.builder()
				.username("cuneyt")
				.email("test@gmail.com")
				.password("123456")
				.build();

		User mockUser = User.builder()
				.username("cuneyt")
				.email("test@gmail.com")
				.password("encoded_password")
				.build();

		given(authService.register(ArgumentMatchers.any(RegisterRequest.class))).willReturn(mockUser);

		ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)));

		response.andExpect(status().isOk());
	}

	@Test
	public void AuthController_LoginUser_ReturnAuthResponse() throws Exception {

		LoginRequest loginRequest = LoginRequest.builder()
				.email("test@gmail.com")
				.password("123456")
				.build();

		AuthResponse mockResponse = new AuthResponse("mock_jwt_token_123", "test@gmail.com");

		given(authService.login(ArgumentMatchers.any(LoginRequest.class))).willReturn(mockResponse);

		ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)));

		response.andExpect(status().isOk())

				.andExpect(jsonPath("$.token").value("mock_jwt_token_123"))
				.andExpect(jsonPath("$.email").value("test@gmail.com"));
	}

}

