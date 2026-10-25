package com.vicky.docqora.controller;

import com.vicky.docqora.Service.RagService;
import com.vicky.docqora.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.node.StringNode;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController
{
    private final RagService ragService;

    @PostMapping("/query")
    @Operation(summary = "Ask question againsts all documentsor a specific document with citation")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askQuestion(@Valid @RequestBody ChatRequestDto chatRequestDto){
        ChatResponseDto chatResponseDto = ragService.askQuestions(chatRequestDto);
        return ResponseEntity.ok(
                ApiResponse.<ChatResponseDto>builder()
                        .success(true)
                        .message(null)
                        .data(chatResponseDto)
                        .time(LocalDateTime.now())
                        .build()
        );

    }

    @PostMapping("/stream")
    public Flux<String> streamQuestion(@Valid @RequestBody ChatRequestDto chatRequestDto){
        return ragService.streamQuestionAnswer(chatRequestDto);
    }

    @PostMapping("/search/similarity")
    public ResponseEntity<ApiResponse<SearchResultDto>> searchSimilarity(@Valid @RequestBody SearchRequestDto searchRequestDto){
        SearchResultDto searchResultDto = ragService.searchSimilarChunks(searchRequestDto);
        return ResponseEntity.ok(
                ApiResponse.<SearchResultDto>builder()
                        .success(true)
                        .message(null)
                        .data(searchResultDto)
                        .time(LocalDateTime.now())
                        .build()
        );
    }

}
