package com.taghub.tag_server.controller;

import com.taghub.tag_server.entity.TextData;
import com.taghub.tag_server.service.TextDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/texts")
@RequiredArgsConstructor
public class TextDataController {

	private final TextDataService textDataService;

	@PostMapping(value = "/{roomId}/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadFile(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String adminIdString,
			@RequestParam("file") MultipartFile file
	) {
		UUID adminId = UUID.fromString(adminIdString);

		// Servis metodunu çağırıyoruz
		textDataService.uploadTextsFromFile(roomId, adminId, file);

		return ResponseEntity.ok("Dosya başarıyla işlendi ve içerisindeki metinler sisteme kaydedildi.");
	}

	@GetMapping("/{roomId}/next-batch")
	public ResponseEntity<List<TextData>> getNextBatch(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString,
			@RequestParam(defaultValue = "10") int batchSize
	) {
		UUID userId = UUID.fromString(userIdString);

		List<TextData> nextBatch = textDataService.getNextBatchForUser(roomId, userId, batchSize);

		return ResponseEntity.ok(nextBatch);
	}

	@PostMapping("/{roomId}/close")
	public ResponseEntity<String> closeLabeling(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String adminIdString
	) {
		UUID adminId = UUID.fromString(adminIdString);

		textDataService.closeLabelingAndTallyVotes(roomId, adminId);

		return ResponseEntity.ok("Oylama başarıyla kapatıldı ve çoğunluk oylarına göre sonuçlar hesaplandı.");
	}


	@GetMapping("/{roomId}/export")
	public ResponseEntity<byte[]> exportDataset(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString
	) {
		UUID userId = UUID.fromString(userIdString);

		byte[] csvData = textDataService.exportRoomDatasetToCsv(roomId, userId);

		String fileName = "dataset_" + roomId.toString().substring(0, 8) + ".csv";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(csvData);
	}
}