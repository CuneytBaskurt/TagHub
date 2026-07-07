package com.taghub.auth_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 50)
	private String username;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String password;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "reset_code", length = 6)
	private String resetCode;

	@Column(name = "reset_code_expires_at")
	private LocalDateTime resetCodeExpiresAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public boolean isAccountNonExpired() { return true; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() { return true; }
}
