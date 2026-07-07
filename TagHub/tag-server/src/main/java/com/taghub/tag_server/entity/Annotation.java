package com.taghub.tag_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "annotations", indexes = {
		@Index(name = "idx_annotation_room", columnList = "room_id"),
		@Index(name = "idx_annotation_text_user", columnList = "text_id, user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annotation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "room_id", nullable = false)
	private UUID roomId;

	@Column(name = "text_id", nullable = false)
	private UUID textId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "label_index", nullable = false)
	private Integer labelIndex;

	@CreationTimestamp
	@Column(name = "labeled_at", updatable = false)
	private LocalDateTime labeledAt;
}
