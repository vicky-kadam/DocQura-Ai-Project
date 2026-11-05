package com.vicky.docqora.controller;

import com.vicky.docqora.dto.ApiResponse;
import com.vicky.docqora.dto.ChatRequestDto;
import com.vicky.docqora.dto.DocumentMetaDto;
import com.vicky.docqora.dto.DocumentResponseDto;
import com.vicky.docqora.Service.DocumentMetaDataService;
import com.vicky.docqora.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@EnableMethodSecurity
@Tag(
        name="Document Management",
        description = "Endpoint are managing listing and mapping for document and their emmbeding vectors"
)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentMetaDataService documentMetaDataService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            description = "Here we can add multiple docs file and other format files "
    )
    public ResponseEntity<ApiResponse<DocumentResponseDto>> upload(@RequestParam("file") MultipartFile file, Authentication authentication)
    {

        User user = (User) authentication.getPrincipal();

        log.info("document uploading started");
        DocumentResponseDto documentResponseDto = documentMetaDataService.uploadAndProcess(file,user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentResponseDto>builder()
                        .success(true)
                        .data(documentResponseDto)
                        .time(LocalDateTime.now())
                        .message("Document upload and indexed successfully")
                        .build());

    }
    @PostMapping(value = "/upload-multiple",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "upload and index multiple file simultaneously",
            description = "Here we can add multiple docs file and other format files "
    )
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultipleFile(@RequestParam("files") List<MultipartFile> file,Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<DocumentResponseDto> documentResponseDtos =  documentMetaDataService.uploadMultipleAndProcess(file,user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                       ApiResponse.<List<DocumentResponseDto>>builder()
                               .message("document uploaded successfully")
                               .success(true)
                               .time(LocalDateTime.now())
                               .data(documentResponseDtos)
                               .build()

                );

    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all stored documents"
            )
    public ResponseEntity<ApiResponse<List<DocumentMetaDto>>> getAllDocuments(){
        List<DocumentMetaDto> document =  documentMetaDataService.getAllDocuments();
        return ResponseEntity.ok(
                ApiResponse.<List<DocumentMetaDto>>builder()
                        .message("document fetch successfully from the database")
                        .success(true)
                        .time(LocalDateTime.now())
                        .data(document)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentMetaDto>>> getAllDocumentsByUser(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<DocumentMetaDto> documents = documentMetaDataService.getAllDocumentsByUser(user);
        return ResponseEntity.ok(
                ApiResponse.<List<DocumentMetaDto>>builder()
                        .message("document fetch successfully by user")
                        .success(true)
                        .time(LocalDateTime.now())
                        .data(documents)
                        .build()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentMetaDto>> getDocumentById(@PathVariable UUID id){
        DocumentMetaDto documentResponseDto = documentMetaDataService.getDocumentById(id);
        return ResponseEntity.ok(
                ApiResponse.<DocumentMetaDto>builder()
                        .message("document fetch successfully from the database")
                        .success(true)
                        .time(LocalDateTime.now())
                        .data(documentResponseDto)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentById(@PathVariable UUID id){
        documentMetaDataService.deleteById(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("document deleted SuccessFully  successfully from the database")
                        .success(true)
                        .time(LocalDateTime.now())
                        .data(null)
                        .build()
        );
    }







}
