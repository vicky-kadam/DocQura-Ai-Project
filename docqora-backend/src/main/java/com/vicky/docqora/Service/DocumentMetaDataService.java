package com.vicky.docqora.Service;

import com.vicky.docqora.dto.DocumentResponseDto;
import com.vicky.docqora.entity.DocumentMetadata;
import com.vicky.docqora.entity.DocumentStatus;
import com.vicky.docqora.repository.DocumentMetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentMetaDataService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentMetaDataService.class);

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
}
