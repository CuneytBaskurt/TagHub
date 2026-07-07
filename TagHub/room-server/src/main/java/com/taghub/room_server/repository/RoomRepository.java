package com.taghub.room_server.repository;

import com.taghub.room_server.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
	Optional<Room> findByInviteToken(String inviteToken);
	@Query("SELECT r FROM Room r WHERE r.status = 'ACTIVE' AND r.id IN " +
			"(SELECT rp.roomId FROM RoomParticipant rp WHERE rp.userId = :userId AND rp.status = 'ACTIVE')")
	List<Room> findActiveRoomsByUserId(@Param("userId") UUID userId);
}

