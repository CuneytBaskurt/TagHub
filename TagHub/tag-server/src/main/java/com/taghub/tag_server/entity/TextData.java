package com.taghub.tag_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "text_data", indexes = {
		@Index(name = "idx_text_room", columnList = "room_id"),
		@Index(name = "idx_text_status", columnList = "is_fully_labeled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextData {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "room_id", nullable = false)
	private UUID roomId;

	@Column(columnDefinition = "TEXT", nullable = false)
	@ColumnTransformer(
			write = "pgp_sym_encrypt(?, 'taghub_gizli_anahtar_2026')",
			read = "pgp_sym_decrypt(content::bytea, 'taghub_gizli_anahtar_2026')"
	)
	private String content;

	@Column(columnDefinition = "TEXT")
	private String metadata;

	@Column(name = "is_fully_labeled", nullable = false)
	private boolean isFullyLabeled = false;

	@Column(name = "final_label_index")
	private Integer finalLabelIndex;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
