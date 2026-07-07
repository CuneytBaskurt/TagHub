package com.taghub.room_server.entity;

import com.taghub.room_server.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "owner_id", nullable = false)
	private UUID owner_id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "invite_token", nullable = false)
	private String inviteToken;

	@Column(name = "capacity", nullable = false)
	private int capacity;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private RoomStatus status;

	@ElementCollection
	@CollectionTable(name = "room_labels", joinColumns = @JoinColumn(name = "room_id"))
	@Column(name = "labels")
	private List<String> labels;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime created_at = LocalDateTime.now();
}
