package com.vicky.docqora.Service;


import com.vicky.docqora.exception.DocumentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;


@Service
@Slf4j
public class DocumentParserService {

    public List<Document> parse(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : " ";

        log.info("parsing document: {}, content type: {}", fileName, contentType);

        try {
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public @Nullable String getFilename() {
                    return fileName;
                }
                
                
            };
            
            if (fileName.toLowerCase().endsWith(".pdf") || contentType.contains("pdf")) {
                return parsePdf(resource);
            }
            else{
               return parseGenricfile(resource);
            }
        } catch (IOException e) {
            log.error("failed to read fiel bytes : {}" + fileName, e);
            throw new DocumentProcessingException("could not read upload file : " + fileName, e);
        }
         catch (Exception e) {
            log.error("failed to read file bytes: {}", fileName, e);
            throw new DocumentProcessingException("could not read upload file : " + fileName, e);
        }




    }

    private List<Document> parsePdf(Resource resource) {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().
                withPageBottomMargin(0)
                .withPageTopMargin(0)
                .build();

        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, config);
        return pagePdfDocumentReader.read();
    }

    private List<Document> parseGenricfile(Resource resource) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        return tikaDocumentReader.read();
    }
}
