package com.gateway_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Autowired
	private RouteValidator validator;

	@Autowired
	@Lazy
	private JwtUtil jwtUtil;

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			if (validator.isSecured.test(exchange.getRequest())) {

				if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
					logger.warn("Gümrük Engeli: Authorization header eksik! Path: {}", exchange.getRequest().getURI().getPath());
					return onError(exchange, "Yetkilendirme başlığı eksik (Missing Authorization Header)", HttpStatus.UNAUTHORIZED);
				}

				String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					authHeader = authHeader.substring(7);
				}

				try {
					jwtUtil.validateToken(authHeader);

					String userId = jwtUtil.extractUserId(authHeader);
					logger.info("Başarılı geçiş: Token doğrulandı. Kullanıcı ID: {}", userId);

					ServerHttpRequest mutatedRequest = exchange.getRequest()
							.mutate()
							.header("X-User-Id", userId)
							.build();

					return chain.filter(exchange.mutate().request(mutatedRequest).build());

				} catch (Exception e) {
					logger.warn("Yetkisiz giriş denemesi: Geçersiz token! Hata: {}", e.getMessage());
					return onError(exchange, "Geçersiz veya süresi dolmuş token (Invalid Token)", HttpStatus.UNAUTHORIZED);
				}
			}

			return chain.filter(exchange);
		});
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String errorBody = String.format(
				"{\"status\": %d, \"message\": \"%s\", \"timestamp\": %d}",
				httpStatus.value(), err, System.currentTimeMillis()
		);

		DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
		return response.writeWith(Mono.just(buffer));
	}

	public static class Config {}
}