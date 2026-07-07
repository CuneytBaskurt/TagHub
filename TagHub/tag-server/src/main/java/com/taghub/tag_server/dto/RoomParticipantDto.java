package com.taghub.tag_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipantDto {
	private Long id;
	private UUID roomId;
	private UUID userId;
	private String role;
	private String status;
}
