package com.vicky.docqora.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SearchRequestDto {
    @NotBlank(message = "Query cannot be empty")
    private String query;
    private UUID documentID;
    private Integer topK;
    private Double similaritySearch;


}
