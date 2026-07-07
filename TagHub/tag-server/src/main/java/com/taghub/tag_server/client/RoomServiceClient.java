package com.taghub.tag_server.client;

import com.taghub.tag_server.dto.RoomParticipantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "room-server", path = "/api/v1/room")
public interface RoomServiceClient {

	@GetMapping("/{roomId}/participants/{userId}/check")
	RoomParticipantDto checkParticipantStatus(
			@PathVariable("roomId") UUID roomId,
			@PathVariable("userId") UUID userId
	);
}
