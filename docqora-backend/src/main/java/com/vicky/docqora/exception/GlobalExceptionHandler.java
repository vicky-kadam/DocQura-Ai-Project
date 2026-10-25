package com.vicky.docqora.exception;


import com.vicky.docqora.dto.ApiResponse;
import com.vicky.docqora.exception.DocumentProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleDocumentProcessingException(
            DocumentProcessingException e) {

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<?> documentNotFoundException(DocumentNotFoundException e){
        ApiResponse apiResponse = ApiResponse.builder()
                .success(false)
                .data(null)
                .message(e.getMessage())
                .time(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

    }
}

