package com.vicky.docqora.repository;

import com.vicky.docqora.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentMetaDataRepository extends JpaRepository<DocumentMetadata, UUID> {

}
