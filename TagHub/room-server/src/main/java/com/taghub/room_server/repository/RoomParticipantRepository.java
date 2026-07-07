package com.taghub.room_server.repository;

import com.taghub.room_server.entity.RoomParticipant;
import com.taghub.room_server.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, UUID> {
	long countByRoomIdAndStatus(UUID roomId, ParticipantStatus status);

	boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);
	Optional<RoomParticipant> findByRoomIdAndUserId(UUID roomId, UUID userId);
	List<RoomParticipant> findAllByRoomIdAndStatus(UUID roomId, ParticipantStatus status);

	@Modifying
	@Query("UPDATE RoomParticipant rp SET rp.status = 'ROOM_DELETED' WHERE rp.roomId = :roomId AND rp.status = 'ACTIVE'")
	void updateStatusToRoomDeletedByRoomId(@Param("roomId") UUID roomId);
}
