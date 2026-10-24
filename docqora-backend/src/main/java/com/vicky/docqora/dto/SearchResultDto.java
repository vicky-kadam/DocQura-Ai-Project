package com.vicky.docqora.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SearchResultDto {
    private String query;
    private int totalMatches;
    private List<CitationDto> matches;

}
