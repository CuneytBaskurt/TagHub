package com.gateway_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Lazy
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;

	public void validateToken(final String token) {
		Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token);
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String extractUserId(final String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("userId", String.class);
	}
}

