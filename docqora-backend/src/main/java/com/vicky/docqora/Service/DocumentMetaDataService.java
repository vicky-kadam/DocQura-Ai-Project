package com.vicky.docqora.Service;

import com.vicky.docqora.dto.DocumentMetaDto;
import com.vicky.docqora.dto.DocumentResponseDto;
import com.vicky.docqora.entity.DocumentMetadata;
import com.vicky.docqora.entity.DocumentStatus;
import com.vicky.docqora.exception.DocumentNotFoundException;
import com.vicky.docqora.mapper.DocumentMapper;
import com.vicky.docqora.repository.DocumentMetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentMetaDataService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentMetaDataService.class);
    private final DocumentMapper documentMapper;
    private final JdbcTemplate jdbcTemplate;

    private final DocumentMetaDataRepository documentMetaDataRepository;
    private final DocumentParserService documentParserService;
    private final DocumentIngestionService documentIngestionService;

    public DocumentResponseDto uploadAndProcess(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        DocumentMetadata metadata = DocumentMetadata.builder()
                .fileName(fileName)
                .contentType(contentType)
                .fileSize(file.getSize())
                .status(DocumentStatus.UPLOADING)
                .build();

        DocumentMetadata savedMetadata = documentMetaDataRepository.save(metadata);
        try {
            savedMetadata.setStatus(DocumentStatus.PROCESSING);
            documentMetaDataRepository.save(savedMetadata);

            List<Document> parsedDocuments = documentParserService.parse(file);
            int chunksCreated = documentIngestionService.ingest(savedMetadata, parsedDocuments);

            savedMetadata.setTotalChunks(chunksCreated);
            savedMetadata.setStatus(DocumentStatus.INDEXED);
            documentMetaDataRepository.save(savedMetadata);

            return DocumentResponseDto.builder()
                    .id(savedMetadata.getId())
                    .fileName(savedMetadata.getFileName())
                    .fileSize(savedMetadata.getFileSize())
                    .status(savedMetadata.getStatus())
                    .chunksCreated(chunksCreated)
                    .message("Document parsed and indexed successfully")
                    .build();
        } catch (RuntimeException exception) {
            savedMetadata.setStatus(DocumentStatus.FAILED);
            savedMetadata.setErrorMessage(exception.getMessage());
            documentMetaDataRepository.save(savedMetadata);
            logger.error("Failed to process document {}", savedMetadata.getId(), exception);
            throw exception;
        }
    }

    public List<DocumentResponseDto> uploadMultipleAndProcess(List<MultipartFile> file) {
        List<DocumentResponseDto> responseDtos = new ArrayList<>();
        for (MultipartFile f : file) {
            responseDtos.add(uploadAndProcess(f));
        }
        return responseDtos;
    }

    public List<DocumentMetaDto> getAllDocuments() {
        List<DocumentMetadata> all = documentMetaDataRepository.findAllByOrderByCreatedAtDesc();

        return all.stream()
                .map(documentMapper::toDto)
                .toList();
    }

    public DocumentMetaDto getDocumentById(UUID id) {
        if (!documentMetaDataRepository.existsById(id)){
            throw new DocumentNotFoundException("Document not found with this id" + id);
        }
        DocumentMetadata documentMetadata = documentMetaDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return documentMapper.toDto(documentMetadata);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!documentMetaDataRepository.existsById(id)){
            throw new DocumentNotFoundException("Document not found with this id" + id);
        }
        DocumentMetadata documentMetadata = documentMetaDataRepository.findById(id).orElseThrow(() -> new RuntimeException("Document with given id not found !!"));


        documentMetaDataRepository.delete(documentMetadata);
        //delete the vector entries
        try {
            String deleteVectorsSql = "DELETE FROM vector_store WHERE metadata->>'documentId' = ?";
            int deletedCount = jdbcTemplate.update(deleteVectorsSql, id.toString());
            logger.info("Deleted {} vector chunks for document id {} ", deletedCount, id);

        } catch (Exception e) {
            logger.warn("cloud not delete vectors from vector store directly: {}", e.getMessage());
        }

    }
    }

