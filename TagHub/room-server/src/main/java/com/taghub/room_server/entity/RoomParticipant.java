package com.taghub.room_server.entity;

import com.taghub.room_server.enums.ParticipantRole;
import com.taghub.room_server.enums.ParticipantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "room_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_id", nullable = false)
	private UUID roomId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ParticipantStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private ParticipantRole role;

	@Column(name = "joined_at")
	private LocalDateTime joinedAt;

	@PrePersist
	protected void onCreate() {
		this.joinedAt = LocalDateTime.now();
	}
}
