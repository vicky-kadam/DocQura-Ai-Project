package com.vicky.docqora.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ChatResponseDto {
    private String answer;
    private String conversationId;
    private List<CitationDto> citations;
    private Long responseTime;
}
