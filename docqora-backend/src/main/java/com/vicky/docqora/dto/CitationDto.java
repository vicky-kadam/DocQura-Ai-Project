package com.vicky.docqora.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CitationDto {
    private UUID documentId;
    private String fileName;
    private Integer chunkIndex;
    private Integer pageNumber;
    private String snippet;
    private Double similarityScore;
    private Map<String, Object> metadata;
}
