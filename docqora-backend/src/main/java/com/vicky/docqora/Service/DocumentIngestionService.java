package com.vicky.docqora.Service;

import com.vicky.docqora.config.AppProperties;
import com.vicky.docqora.entity.DocumentMetadata;
import com.vicky.docqora.entity.DocumentStatus;
import com.vicky.docqora.exception.DocumentProcessingException;
import com.vicky.docqora.repository.DocumentMetaDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final DocumentMetaDataRepository documentMetaDataRepository;
    private final AppProperties appProperties;

    public int ingest(
            DocumentMetadata metadata,
            List<Document> parsedDocuments
    ) {

        log.info(
                "Ingesting document [id={}, name={}, pages={}]",
                metadata.getId(),
                metadata.getFileName(),
                parsedDocuments.size()
        );

        try {
            metadata.setStatus(DocumentStatus.PROCESSING);
            metadata.setTotalPages(parsedDocuments.size());

            // Step 1: Check whether extracted documents are empty
            if (parsedDocuments.isEmpty()) {

                metadata.setStatus(DocumentStatus.FAILED);
                metadata.setTotalChunks(0);
                metadata.setErrorMessage(
                        "Document is empty or no readable content was extracted."
                );

                documentMetaDataRepository.save(metadata);
                return 0;
            }

            // Step 2: Configure the text splitter
            TokenTextSplitter tokenTextSplitter =
                    TokenTextSplitter.builder()
                            .withChunkSize(
                                    appProperties.getRag().getChunkSize()
                            )
                            .withMinChunkLengthToEmbed(
                                    appProperties.getRag()
                                            .getMinChunkLengthToEmbed()
                            )
                            .withMaxNumChunks(
                                    appProperties.getRag().getMaxNumChunks()
                            )
                            .withKeepSeparator(true)
                            .build();

            // Step 3: Split documents into chunks
            List<Document> chunks =
                    tokenTextSplitter.apply(parsedDocuments);

            if (chunks.isEmpty()) {

                metadata.setStatus(DocumentStatus.FAILED);
                metadata.setTotalChunks(0);
                metadata.setErrorMessage(
                        "No usable text chunks were generated from the document."
                );

                documentMetaDataRepository.save(metadata);
                return 0;
            }

            // Step 4: Add metadata to every chunk
            List<Document> enrichedChunks = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {

                Document chunk = chunks.get(i);

                Map<String, Object> enrichedMetadata =
                        new HashMap<>(chunk.getMetadata());

                enrichedMetadata.put(
                        "documentId",
                        metadata.getId().toString()
                );

                enrichedMetadata.put(
                        "fileName",
                        metadata.getFileName()
                );

                // kept for chunks/clients that still read the old lower-case key
                enrichedMetadata.put(
                        "filename",
                        metadata.getFileName()
                );

                enrichedMetadata.put(
                        "contentType",
                        metadata.getContentType()
                );
                enrichedMetadata.put(
                        "userId",
                        metadata.getUser().getId().toString()
                );

                enrichedMetadata.put("chunkIndex", i);

                Object pageNumber =
                        chunk.getMetadata().get("page_number");

                if (pageNumber == null) {
                    pageNumber =
                            chunk.getMetadata().get("pageNumber");
                }

                if (pageNumber != null) {
                    enrichedMetadata.put("pageNumber", pageNumber);
                }

                Document enrichedDocument =
                        new Document(
                                chunk.getText(),
                                enrichedMetadata
                        );

                enrichedChunks.add(enrichedDocument);
            }

            // Step 5: Save all chunks to the vector store once
            log.info(
                    "Writing {} chunks to vector store for document: {}",
                    enrichedChunks.size(),
                    metadata.getFileName()
            );

            vectorStore.add(enrichedChunks);

            // Step 6: Update document metadata
            int totalChunks = enrichedChunks.size();

            metadata.setStatus(DocumentStatus.INDEXED);
            metadata.setErrorMessage(null);
            metadata.setTotalChunks(totalChunks);

            documentMetaDataRepository.save(metadata);

            log.info(
                    "Document indexed successfully: name={}, chunks={}",
                    metadata.getFileName(),
                    totalChunks
            );

            // Step 7: Return the actual number of chunks
            return totalChunks;

        } catch (Exception e) {

            log.error(
                    "Error occurred while ingesting document: {}",
                    metadata.getFileName(),
                    e
            );

            metadata.setStatus(DocumentStatus.FAILED);
            metadata.setErrorMessage(e.getMessage());

            documentMetaDataRepository.save(metadata);

            throw new DocumentProcessingException(
                    "Failed to index document: " + e.getMessage(),
                    e
            );
        }
    }
}

