package com.vicky.docqora.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {


    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                       You are Docqora, an intelligent document question-answering assistant. Answer user questions using only the provided document context. Provide clear, accurate, and well-explained answers in 1-2 sentences. Use simple, natural language and respond in the user's language. Never invent information or add unsupported facts. If the answer is not available in the context, clearly state that the provided document does not contain enough information.
            """)
                .build();
    }

    @Bean
    public OpenAPI docqoraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Docqora Backend API")
                        .description("API documentation for Docqora - AI-powered document Q&A backend")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Vicky")
                                .email("vicky-vickykadam@gmail.com")
                                .url("https://your-website.com")));

    }
}
