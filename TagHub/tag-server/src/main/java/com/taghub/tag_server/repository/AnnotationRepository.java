package com.taghub.tag_server.repository;

import com.taghub.tag_server.entity.Annotation;
import com.taghub.tag_server.entity.TextData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, UUID> {

	boolean existsByTextIdAndUserId(UUID textId, UUID userId);

	List<Annotation> findAllByRoomId(UUID roomId);
}

