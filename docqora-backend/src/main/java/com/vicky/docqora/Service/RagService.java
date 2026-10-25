package com.vicky.docqora.Service;

import com.vicky.docqora.config.AppProperties;
import com.vicky.docqora.dto.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class RagService {
    private final VectorStore vectorStore;
    private static final String NOT_FOUND_MSG = "Sorry, I couldn't find the answer to this in the document. Try asking something about the content of the uploaded file.";
    private final AppProperties appProperties;
    private final ChatClient chatClient;

    public ChatResponseDto askQuestions(ChatRequestDto chatRequestDto){
        long startTime = System.currentTimeMillis();
        log.info("Processing quesy: '{}', scoped documentId: '{}'",chatRequestDto.getQuestion(),chatRequestDto.getDocumentId());
        List<Document> similarDocuments = this.retrieveRelevantDocuments(
                chatRequestDto.getQuestion(),
                chatRequestDto.getDocumentId(),
                chatRequestDto.getTopK(),
                chatRequestDto.getMinSimilarity());

        List<CitationDto> citationDto = similarDocuments.stream()
                .map(this::mapToCitation)
                .toList();

        String contextText = buildContextText(similarDocuments);
        String prompt = buildPrompt(chatRequestDto.getQuestion(), contextText);
         String answer = this.chatClient.prompt()
                .user(prompt)
                .call()
                .content();

         long responseTime = System.currentTimeMillis() - startTime;
         log.info("Completed Q&A in {} as with {} citations", responseTime,citationDto.size());
         
         return  ChatResponseDto.builder()
                 .answer(answer)
                 .conversationId(chatRequestDto.getConversationId() != null ? chatRequestDto.getConversationId() : UUID.randomUUID().toString())
                 .citations(citationDto)
                 .responseTime(responseTime)
                 .build();
         
         



    }
    public Flux<String>streamQuestionAnswer(ChatRequestDto chatRequestDto){
        List<Document> relevantDocuments = retrieveRelevantDocuments(
                chatRequestDto.getQuestion(),
                chatRequestDto.getDocumentId(),
                chatRequestDto.getTopK(),
                chatRequestDto.getMinSimilarity()
        );
        String context = buildContextText(relevantDocuments);
        String prompt = buildPrompt(chatRequestDto.getQuestion(), context);
        return this.chatClient.prompt()
                .user(prompt)
                .stream()
                .content();

    }

    private String buildPrompt(@NotBlank(message = "Question cannot be empty") String question,
                               String contextText) {
        return String.format("""
            Read the text below and answer the question using only this text.
            Answer in 1 or 2 short sentences.

            Text:
            %s

            Question: %s
            Answer:
            """, contextText, question);
    }
    private String buildContextText(List<Document> similarDocuments) {
        if(similarDocuments == null || similarDocuments.isEmpty()){
            return "";
        }
        return similarDocuments.stream()
                .map(doc ->{
                    String fileName = String.valueOf(doc.getMetadata().getOrDefault("fileName", "Unknown File"));
                    Object page = doc.getMetadata().getOrDefault("pageNumber",doc.getMetadata().getOrDefault("page_number","N/A"));
                    return String.format("[Source: %s | Page: %s]%n%s", fileName, page, doc.getText());
                })
                .collect(Collectors.joining("\n\n--\n\n"));


    }

    public SearchResultDto searchSimilarChunks(SearchRequestDto request) {
        List<Document> documents = retrieveRelevantDocuments(
                request.getQuery(),
                request.getDocumentID(),
                request.getTopK(),
                request.getSimilaritySearch()
        );

        List<CitationDto> citations = documents.stream()
                .map(this::mapToCitation)
                .toList();
        return SearchResultDto.builder()
                .query(request.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();

    }

    private CitationDto mapToCitation(Document document) {
        Map<String,Object> meta = document.getMetadata();
//        UUID docId = null;
//        if((boolean) meta.get("documentId" !=  null)){
//            try {
//                docId  = UUID.fromString(meta.get("documentID").toString());
//
//            }
//
//            catch (Exception e) {
//                log.error("Error while parsing document ID", e);
//            }
//
//        }
        UUID docId = null;
        Object rawId = meta.get("documentId") != null ? meta.get("documentId") : meta.get("documentID");
        if (rawId != null) {
            try {
                docId = UUID.fromString(rawId.toString());
            } catch (Exception e) {
                log.error("Error while parsing document ID", e);
            }
        }

        Integer chunkIndex = null;
        if (meta.get("chunkIndex") instanceof Number n){
            chunkIndex = n.intValue();
        }

        Integer pageNumber = null;
        if (meta.get("pageNumber") instanceof Number n){
            pageNumber = n.intValue();
        }
        else if (meta.get("page_number") instanceof Number n){
            pageNumber = n.intValue();
        }

        Double score = null;
        if (meta.get("score") instanceof Number n){
            score = 1.0 - n.doubleValue();
        }
        return CitationDto.builder()
                .documentId(docId)
                .chunkIndex(chunkIndex)
                .pageNumber(pageNumber)
                .snippet(document.getText())
                .similarityScore(score)
                .metadata(meta)
                .build();
    }

    private List<Document> retrieveRelevantDocuments(@NotBlank(message = "Query cannot be empty") String query, UUID documentID, Integer topK, Double similaritySearch) {
        int effectiveTopK = (topK != null && topK > 0) ? topK : appProperties.getRag().getTopK();
        double effectiveSimilarity = (similaritySearch != null && similaritySearch > 0) ? similaritySearch : appProperties.getRag().getSimilarityThreshold();

        SearchRequest.Builder searchRequest = SearchRequest.builder()
                .query(query)
                .topK(effectiveTopK);

        if (effectiveSimilarity > 0.0) {
            searchRequest.similarityThreshold(effectiveSimilarity);
        }
        if (documentID != null) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            searchRequest.filterExpression(b.eq("documentId", documentID.toString()).build());;
        }

    try {
        List<Document> documents = vectorStore.similaritySearch(searchRequest.build());
        return documents;
    } catch (Exception e) {
        log.error("Error while searching for similar documents", e);
        return List.of();
    }
    }

}
