package com.vicky.docqora.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ChatRequestDto {
    @NotBlank(message = "Question cannot be empty")
    private String question;
    private UUID documentId;
    private Integer topK;
    private Double minSimilarity;
    private String conversationId;
}
