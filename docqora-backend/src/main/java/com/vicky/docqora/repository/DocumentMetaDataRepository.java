package com.vicky.docqora.repository;

import com.vicky.docqora.entity.DocumentMetadata;
import com.vicky.docqora.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentMetaDataRepository extends JpaRepository<DocumentMetadata, UUID> {
    List<DocumentMetadata> findAllByOrderByCreatedAtDesc();
    List<DocumentMetadata> findByStatus(DocumentStatus status);
    boolean existsById(UUID id);

}
