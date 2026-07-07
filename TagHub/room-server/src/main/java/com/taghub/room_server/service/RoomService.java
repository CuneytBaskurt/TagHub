package com.taghub.room_server.service;

import com.taghub.room_server.entity.Room;
import com.taghub.room_server.entity.RoomParticipant;
import com.taghub.room_server.enums.ParticipantRole;
import com.taghub.room_server.enums.ParticipantStatus;
import com.taghub.room_server.enums.RoomStatus;
import com.taghub.room_server.repository.RoomParticipantRepository;
import com.taghub.room_server.repository.RoomRepository;
import com.taghub.room_server.request.RoomRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {


	private final RoomRepository roomRepository;
	private final RoomParticipantRepository roomParticipantRepository;
	private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

	@Transactional
	public Room createRoom(RoomRequest request, UUID ownerId) {

		String generatedToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		Room room = Room.builder()
				.name(request.getName())
				.capacity(request.getCapacity())
				.labels(request.getLabels())
				.inviteToken(generatedToken)
				.owner_id(ownerId)
				.status(RoomStatus.ACTIVE)
				.created_at(LocalDateTime.now())
				.build();

		Room savedRoom = roomRepository.save(room);

		RoomParticipant adminParticipant = RoomParticipant.builder()
				.roomId(savedRoom.getId())
				.userId(ownerId)
				.role(ParticipantRole.ADMIN)
				.status(ParticipantStatus.ACTIVE)
				.build();

		roomParticipantRepository.save(adminParticipant);

		logger.info("New room has created successfully: {}", room.getName());
		return savedRoom;
	}

	@Transactional
	public Room updateRoom(UUID id, String name, int capacity, List<String> labels){

		Room existingRoom = roomRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Güncellenmek istenen oda bulunamadı!"));

		existingRoom.setName(name);
		existingRoom.setCapacity(capacity);
		existingRoom.setLabels(labels);

		logger.info("The room updated successfully!");
		return existingRoom;
	}

	@Transactional
	public void deleteRoom(UUID id){
		Room existingRoom = roomRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Silinmek istenen oda bulunamadı!"));

		if (existingRoom.getStatus() == RoomStatus.CLOSED) {
			throw new RuntimeException("Bu oda zaten daha önce silinmiş!");
		}

		existingRoom.setStatus(RoomStatus.CLOSED);

		roomRepository.save(existingRoom);

		roomParticipantRepository.updateStatusToRoomDeletedByRoomId(id);

		logger.info("The room status has been updated to DELETED successfully! {}", id);
	}

	@Transactional
	public RoomParticipant joinRoom(UUID userId, String token) {

		Room existRoom = roomRepository.findByInviteToken(token)
				.orElseThrow(() -> new RuntimeException("Geçersiz davet kodu! Böyle bir oda bulunamadı."));

		if (existRoom.getStatus() == RoomStatus.CLOSED) {
			throw new RuntimeException("Bu oda silinmiştir, içeri giremezsiniz!");
		}

		UUID roomId = existRoom.getId();
		long currentActiveUsers = roomParticipantRepository.countByRoomIdAndStatus(roomId, ParticipantStatus.ACTIVE);
		if (currentActiveUsers >= existRoom.getCapacity()) {
			throw new RuntimeException("Oda kapasitesi tamamen dolu, içeri giremezsiniz!");
		}

		Optional<RoomParticipant> existingParticipant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId);

		if (existingParticipant.isPresent()) {
			RoomParticipant participant = existingParticipant.get();

			if (participant.getStatus() == ParticipantStatus.ACTIVE) {
				throw new RuntimeException("Kullanıcı zaten odada!");
			}

			if (participant.getStatus() == ParticipantStatus.KICKED) {
				throw new RuntimeException("Bu odadan yönetici tarafından atıldınız! Tekrar katılamazsınız.");
			}

			if (participant.getStatus() == ParticipantStatus.LEFT) {
				participant.setStatus(ParticipantStatus.ACTIVE);
				roomParticipantRepository.save(participant);
				logger.info("User rejoined successfully: {}", participant.getId());
				return participant;
			}
		}

		RoomParticipant roomParticipant = RoomParticipant.builder()
				.roomId(roomId)
				.userId(userId)
				.role(ParticipantRole.USER)
				.status(ParticipantStatus.ACTIVE)
				.build();

		roomParticipantRepository.save(roomParticipant);

		logger.info("New user joined successfully. Participant ID: {}", roomParticipant.getId());
		return roomParticipant;
	}

	@Transactional
	public RoomParticipant leaveRoom(UUID userId, UUID roomId){

		RoomParticipant participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
				.orElseThrow(() -> new RuntimeException("Bu odada zaten bulunmuyorsunuz!"));

		if (participant.getStatus() == ParticipantStatus.LEFT) {
			throw new RuntimeException("Zaten bu odadan ayrılmışsınız!");
		}

		participant.setStatus(ParticipantStatus.LEFT);

		roomParticipantRepository.save(participant);

		logger.info("User {} left the room successfully: {}", userId, roomId);
		return participant;
	}

	@Transactional(readOnly = true)
	public Room enterRoom(UUID userId, UUID roomId){

		Room existRoom = roomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Böyle bir oda bulunamadı!"));

		if (existRoom.getStatus() == RoomStatus.CLOSED) {
			throw new RuntimeException("Bu oda silinmiştir, içeri giremezsiniz!");
		}

		RoomParticipant participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
				.orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

		if(participant.getStatus() != ParticipantStatus.ACTIVE){
			throw new RuntimeException("Bu odadan ayrılmışsınız!");
		}

		return existRoom;
	}

	@Transactional(readOnly = true)
	public List<Room> getUserRooms(UUID userId) {
		logger.info("Fetching active rooms for user: {}", userId);

		return roomRepository.findActiveRoomsByUserId(userId);
	}

	@Transactional(readOnly = true)
	public List<RoomParticipant> getRoomParticipants(UUID roomId, UUID requesterId) {

		RoomParticipant requester = roomParticipantRepository.findByRoomIdAndUserId(roomId, requesterId)
				.orElseThrow(() -> new RuntimeException("Bu odaya kayıtlı değilsiniz, katılımcıları göremezsiniz!"));

		if (requester.getStatus() != ParticipantStatus.ACTIVE) {
			throw new RuntimeException("Bu odada aktif değilsiniz, katılımcıları göremezsiniz!");
		}
		return roomParticipantRepository.findAllByRoomIdAndStatus(roomId, ParticipantStatus.ACTIVE);
	}

	@Transactional
	public RoomParticipant kickUser(UUID roomId, UUID adminId, UUID targetUserId) {

		RoomParticipant admin = roomParticipantRepository.findByRoomIdAndUserId(roomId, adminId)
				.orElseThrow(() -> new RuntimeException("Bu odada bulunmuyorsunuz!"));

		if (admin.getStatus() != ParticipantStatus.ACTIVE) {
			throw new RuntimeException("Bu odada aktif değilsiniz, yetki kontrolü yapılamadı!");
		}

		if (admin.getRole() != ParticipantRole.ADMIN && admin.getRole() != ParticipantRole.OWNER) {
			throw new RuntimeException("Bu işlem için yetkiniz yok! Yalnızca yöneticiler kullanıcı atabilir.");
		}

		if (adminId.equals(targetUserId)) {
			throw new RuntimeException("Kendinizi odadan atamazsınız! Odadan ayrılmak için 'Leave' özelliğini kullanın.");
		}

		RoomParticipant target = roomParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId)
				.orElseThrow(() -> new RuntimeException("Atılmak istenen kullanıcı bu odada bulunamadı!"));

		if (target.getStatus() == ParticipantStatus.KICKED) {
			throw new RuntimeException("Kullanıcı zaten odada aktif değil! Güncel durumu: " + target.getStatus());
		}

		target.setStatus(ParticipantStatus.KICKED);
		roomParticipantRepository.save(target);

		logger.warn("User {} has been successfully kicked from room {} by admin {}", targetUserId, roomId, adminId);
		return target;
	}


}

