package com.vicky.docqora.mapper;

import com.vicky.docqora.dto.DocumentMetaDto;
import com.vicky.docqora.entity.DocumentMetadata;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentMapper {

    public DocumentMetaDto toDto(DocumentMetadata entity) {
        return DocumentMetaDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .totalPages(entity.getTotalPages())
                .totalChunks(entity.getTotalChunks())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<DocumentMetaDto> toDtoList(List<DocumentMetadata> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}