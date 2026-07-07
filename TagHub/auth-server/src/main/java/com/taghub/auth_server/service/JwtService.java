package com.taghub.auth_server.service;

import com.taghub.auth_server.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

	@Value("${jwt.secret-key}")
	private String secretKey;

	private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", user.getId());
		claims.put("role", "USER");

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(user.getEmail())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(new Date());
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}

