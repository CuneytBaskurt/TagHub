package com.taghub.tag_server.repository;

import com.taghub.tag_server.entity.TextData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TextDataRepository extends JpaRepository<TextData, UUID> {

	long countByRoomId(UUID roomId);
	long countByRoomIdAndIsFullyLabeled(UUID roomId, boolean isFullyLabeled);

	@Query("SELECT t FROM TextData t WHERE t.roomId = :roomId AND t.isFullyLabeled = false " +
			"AND t.id NOT IN (SELECT a.textId FROM Annotation a WHERE a.userId = :userId)")
	List<TextData> findUnlabeledTextsForUser(@Param("roomId") UUID roomId, @Param("userId") UUID userId, Pageable pageable);

	List<TextData> findAllByRoomIdAndIsFullyLabeled(UUID roomId, boolean isFullyLabeled);
}