package com.vicky.docqora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse<D> {

    private Boolean success;
    private D data;
    private String message;
    private LocalDateTime time;




}