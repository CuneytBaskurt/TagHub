package com.taghub.tag_server.service;

import com.taghub.tag_server.client.RoomServiceClient;
import com.taghub.tag_server.dto.RoomParticipantDto;
import com.taghub.tag_server.entity.Annotation;
import com.taghub.tag_server.entity.TextData;
import com.taghub.tag_server.repository.AnnotationRepository;
import com.taghub.tag_server.repository.TextDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextDataService {

	private final TextDataRepository textDataRepository;
	private final RoomServiceClient roomServiceClient;
	private final AnnotationRepository annotationRepository;


	@Transactional
	public void uploadTextsFromFile(UUID roomId, UUID adminId, MultipartFile file) {
		RoomParticipantDto participant;
		try {
			participant = roomServiceClient.checkParticipantStatus(roomId, adminId);
		} catch (Exception e) {
			log.error("Gümrük kapısındaki gerçek Feign hatası: ", e);
			throw new RuntimeException("Kullanıcı doğrulanırken hata: " + e.getMessage());
		}

		if (!"ACTIVE".equals(participant.getStatus()) ||
				(!"ADMIN".equals(participant.getRole()) && !"OWNER".equals(participant.getRole()))) {
			throw new RuntimeException("Bu işlem için yetkiniz yok!");
		}

		if (file.isEmpty()) {
			throw new RuntimeException("Yüklenen dosya boş olamaz!");
		}

		List<String> extractedTexts = new ArrayList<>();
		String fileName = file.getOriginalFilename();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue; // Boş satırları atla

				if (fileName != null && fileName.endsWith(".csv")) {
					if (line.startsWith("\"") && line.endsWith("\"")) {
						line = line.substring(1, line.length() - 1);
					}
					line = line.replace("\"\"", "\"");
				}

				extractedTexts.add(line);
			}
		} catch (Exception e) {
			throw new RuntimeException("Dosya okunurken bir hata meydana geldi: " + e.getMessage());
		}

		if (extractedTexts.isEmpty()) {
			throw new RuntimeException("Dosya içinde geçerli bir metin bulunamadı!");
		}

		List<TextData> textDataList = extractedTexts.stream()
				.map(content -> TextData.builder()
						.roomId(roomId)
						.content(content)
						.isFullyLabeled(false)
						.build())
				.collect(Collectors.toList());

		textDataRepository.saveAll(textDataList);
		log.info("Dosyadan okunan {} adet metin başarıyla yüklendi. Dosya adı: {}", textDataList.size(), fileName);
	}

	@Transactional(readOnly = true)
	public List<TextData> getNextBatchForUser(UUID roomId, UUID userId, int batchSize) {


		RoomParticipantDto participant;
		try {
			participant = roomServiceClient.checkParticipantStatus(roomId, userId);
		} catch (Exception e) {
			throw new RuntimeException("Kullanıcı doğrulanırken bir hata oluştu veya bu odaya kayıtlı değilsiniz!");
		}

		if (!"ACTIVE".equals(participant.getStatus())) {
			throw new RuntimeException("Bu odada aktif değilsiniz, etiketleme yapamazsınız!");
		}

		Pageable pageable = PageRequest.of(0, batchSize);


		List<TextData> batch = textDataRepository.findUnlabeledTextsForUser(roomId, userId, pageable);

		log.info("User {} requested texts for room {}. Found {} texts.", userId, roomId, batch.size());

		return batch;
	}

	@Transactional
	public void closeLabelingAndTallyVotes(UUID roomId, UUID adminId) {

		RoomParticipantDto participant;
		try {
			participant = roomServiceClient.checkParticipantStatus(roomId, adminId);
		} catch (Exception e) {
			throw new RuntimeException("Kullanıcı doğrulanırken bir hata oluştu!");
		}

		if (!"ADMIN".equals(participant.getRole()) && !"OWNER".equals(participant.getRole())) {
			throw new RuntimeException("Oylama sürecini sadece yöneticiler (ADMIN/OWNER) sonlandırabilir!");
		}

		List<Annotation> allVotes = annotationRepository.findAllByRoomId(roomId);
		if (allVotes.isEmpty()) {
			throw new RuntimeException("Bu odada henüz hiç oy kullanılmamış, süreç sonlandırılamaz!");
		}

		List<TextData> allTexts = textDataRepository.findAll().stream()
				.filter(t -> t.getRoomId().equals(roomId))
				.collect(Collectors.toList());

		Map<UUID, List<Annotation>> votesByText = allVotes.stream()
				.collect(Collectors.groupingBy(Annotation::getTextId));

		for (TextData text : allTexts) {
			List<Annotation> textVotes = votesByText.getOrDefault(text.getId(), Collections.emptyList());

			if (textVotes.isEmpty()) {
				text.setFullyLabeled(true);
				continue;
			}

			Map<Integer, Long> labelCounts = textVotes.stream()
					.collect(Collectors.groupingBy(Annotation::getLabelIndex, Collectors.counting()));

			Integer winningLabel = labelCounts.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.orElse(null);

			text.setFinalLabelIndex(winningLabel);
			text.setFullyLabeled(true);
		}

		textDataRepository.saveAll(allTexts);

		log.info("Room {} labeling process closed. Tallying completed for {} texts by admin {}.", roomId, allTexts.size(), adminId);
	}

	@Transactional(readOnly = true)
	public byte[] exportRoomDatasetToCsv(UUID roomId, UUID userId) {

		RoomParticipantDto participant;
		try {
			participant = roomServiceClient.checkParticipantStatus(roomId, userId);
		} catch (Exception e) {
			throw new RuntimeException("Kullanıcı doğrulanırken bir hata oluştu!");
		}

		if (!"ACTIVE".equals(participant.getStatus()) && !"ROOM_DELETED".equals(participant.getStatus())) {
			throw new RuntimeException("Bu odada aktif değilsiniz, veri setini indiremezsiniz! Mevcut durumunuz: " + participant.getStatus());
		}

		List<TextData> labeledTexts = textDataRepository.findAllByRoomIdAndIsFullyLabeled(roomId, true);
		if (labeledTexts.isEmpty()) {
			throw new RuntimeException("Bu odada henüz etiketlemesi tamamlanmış bir metin bulunamadı!");
		}

		StringBuilder csvBuilder = new StringBuilder();

		csvBuilder.append("text_id,content,final_label\n");

		for (TextData text : labeledTexts) {
			String safeContent = text.getContent();

			if (safeContent != null) {
				safeContent = safeContent.replace("\"", "\"\"");
				safeContent = "\"" + safeContent + "\"";
			} else {
				safeContent = "\"\"";
			}

			csvBuilder.append(text.getId()).append(",")
					.append(safeContent).append(",")
					.append(text.getFinalLabelIndex())
					.append("\n");
		}

		return csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
	}
}