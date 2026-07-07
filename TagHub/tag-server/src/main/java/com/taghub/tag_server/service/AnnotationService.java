package com.taghub.tag_server.service;

import com.taghub.tag_server.client.RoomServiceClient;
import com.taghub.tag_server.dto.RoomParticipantDto;
import com.taghub.tag_server.entity.Annotation;
import com.taghub.tag_server.entity.TextData;
import com.taghub.tag_server.repository.AnnotationRepository;
import com.taghub.tag_server.repository.TextDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationService {

	private final AnnotationRepository annotationRepository;
	private final TextDataRepository textDataRepository;
	private final RoomServiceClient roomServiceClient;

	@Transactional
	public void saveVote(UUID roomId, UUID userId, UUID textId, Integer labelIndex) {

		RoomParticipantDto participant;
		try {
			participant = roomServiceClient.checkParticipantStatus(roomId, userId);
		} catch (Exception e) {
			throw new RuntimeException("Kullanıcı doğrulanırken bir hata oldu veya odaya kayıtlı değilsiniz!");
		}

		if (!"ACTIVE".equals(participant.getStatus())) {
			throw new RuntimeException("Bu odada aktif durumda değilsiniz, oy kullanamazsınız!");
		}

		TextData textData = textDataRepository.findById(textId)
				.orElseThrow(() -> new RuntimeException("Oylanmak istenen metin bulunamadı!"));

		if (textData.isFullyLabeled()) {
			throw new RuntimeException("Bu metnin oylama süreci admin tarafından çoktan kapatılmış!");
		}

		boolean alreadyVoted = annotationRepository.existsByTextIdAndUserId(textId, userId);
		if (alreadyVoted) {
			throw new RuntimeException("Bu metni daha önce zaten etiketlediniz! Mükerrer oy kullanılamaz.");
		}

		Annotation annotation = Annotation.builder()
				.roomId(roomId)
				.textId(textId)
				.userId(userId)
				.labelIndex(labelIndex)
				.build();

		annotationRepository.save(annotation);
		log.info("User {} successfully voted for text {} with label {}", userId, textId, labelIndex);
	}
}
