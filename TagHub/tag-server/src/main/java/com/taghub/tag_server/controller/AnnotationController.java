package com.taghub.tag_server.controller;

import com.taghub.tag_server.dto.VoteRequest;
import com.taghub.tag_server.service.AnnotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
public class AnnotationController {

	private final AnnotationService annotationService;

	@PostMapping("/{roomId}/vote")
	public ResponseEntity<String> vote(
			@PathVariable UUID roomId,
			@RequestHeader("X-User-Id") String userIdString,
			@RequestBody VoteRequest request
	) {
		UUID userId = UUID.fromString(userIdString);

		annotationService.saveVote(roomId, userId, request.getTextId(), request.getLabelIndex());

		return ResponseEntity.ok("Oy başarıyla sisteme işlendi.");
	}
}
