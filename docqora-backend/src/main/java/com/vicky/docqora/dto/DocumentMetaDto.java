package com.vicky.docqora.dto;

import com.vicky.docqora.entity.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DocumentMetaDto {

    private UUID id;


    private String fileName;
    private String contentType;
    private Long fileSize;
    private Integer totalPages;
    private Integer totalChunks;


    private DocumentStatus status;

    private String errorMessage;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
