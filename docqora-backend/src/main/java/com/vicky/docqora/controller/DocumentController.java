package com.vicky.docqora.controller;

import com.vicky.docqora.dto.ApiResponse;
import com.vicky.docqora.dto.DocumentResponseDto;
import com.vicky.docqora.Service.DocumentMetaDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<ApiResponse<DocumentResponseDto>> upload(@RequestParam("file") MultipartFile file){
        DocumentResponseDto documentResponseDto = documentMetaDataService.uploadAndProcess(file);
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
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultipleFile(@RequestParam("files") List<MultipartFile> file){
        List<DocumentResponseDto> documentResponseDtos =  documentMetaDataService.uploadMultipleAndProcess(file);
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


}
