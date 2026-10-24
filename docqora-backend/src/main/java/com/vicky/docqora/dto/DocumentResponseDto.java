package com.vicky.docqora.dto;

import com.vicky.docqora.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {
    private UUID id;
    private String fileName;
    private Long fileSize;
    private DocumentStatus status;
    private Integer chunksCreated;
    private String message;

}
