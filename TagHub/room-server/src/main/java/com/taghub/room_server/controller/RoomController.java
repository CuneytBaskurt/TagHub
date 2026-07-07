package com.taghub.room_server.controller;

import com.taghub.room_server.entity.Room;
import com.taghub.room_server.entity.RoomParticipant;
import com.taghub.room_server.repository.RoomParticipantRepository;
import com.taghub.room_server.request.RoomRequest;
import com.taghub.room_server.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
public class RoomController {

	private final RoomService roomService;
	private final RoomParticipantRepository roomParticipantRepository;

	@PostMapping("/create")
	public ResponseEntity<Room> create(
			@Valid @RequestBody RoomRequest request,
			@RequestHeader("X-User-Id") String userIdString
	){
		UUID userId = UUID.fromString(userIdString);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(roomService.createRoom(request, userId));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Room> update(
			@PathVariable UUID id,
			@Valid @RequestBody RoomRequest request,
			@RequestHeader("X-User-Id") String userIdString
	){
		Room updatedRoom = roomService.updateRoom(id, request.getName(), request.getCapacity(), request.getLabels());

		return ResponseEntity.ok(updatedRoom);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable UUID id
	){
		roomService.deleteRoom(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/join/{token}")
	public ResponseEntity<RoomParticipant> joinRoom(
			@PathVariable String token,
			@RequestHeader("X-User-Id") String userIdString
	){
		UUID userId = UUID.fromString(userIdString);

		RoomParticipant participant = roomService.joinRoom(userId, token);

		return ResponseEntity.ok(participant);
	}

	@PostMapping("/{roomId}/leave")
	public ResponseEntity<RoomParticipant> leave(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString
	){
		UUID userId = UUID.fromString(userIdString);

		RoomParticipant participant = roomService.leaveRoom(userId, roomId);

		return ResponseEntity.ok(participant);
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<Room> enter(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString
	){
		UUID userId = UUID.fromString(userIdString);

		Room room = roomService.enterRoom(userId, roomId);

		return ResponseEntity.ok(room);
	}

	@GetMapping("/my-rooms")
	public ResponseEntity<List<Room>> getMyRooms(
			@RequestHeader("X-User-Id") String userIdString
	) {
		UUID userId = UUID.fromString(userIdString);

		List<Room> myRooms = roomService.getUserRooms(userId);

		return ResponseEntity.ok(myRooms);
	}

	@GetMapping("/{roomId}/participants")
	public ResponseEntity<List<RoomParticipant>> getParticipants(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString
	) {
		UUID userId = UUID.fromString(userIdString);

		List<RoomParticipant> participants = roomService.getRoomParticipants(roomId, userId);

		return ResponseEntity.ok(participants);
	}

	@PostMapping("/{roomId}/kick/{targetUserId}")
	public ResponseEntity<RoomParticipant> kickUser(
			@PathVariable UUID roomId,
			@PathVariable UUID targetUserId,
			@RequestHeader("X-User-Id") String adminIdString
	) {
		UUID adminId = UUID.fromString(adminIdString);

		RoomParticipant kickedParticipant = roomService.kickUser(roomId, adminId, targetUserId);

		return ResponseEntity.ok(kickedParticipant);
	}

	@GetMapping("/{roomId}/participants/{userId}/check")
	public ResponseEntity<RoomParticipant> checkParticipant(
			@PathVariable UUID roomId,
			@PathVariable UUID userId
	) {
		RoomParticipant participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
				.orElseThrow(() -> new RuntimeException("Kullanıcı bu odada bulunamadı!"));

		return ResponseEntity.ok(participant);
	}
}
