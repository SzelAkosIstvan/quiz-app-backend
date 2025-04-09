package com.vduzzle.QuizApp.Service;

import com.vduzzle.QuizApp.dbo.ChatGPTRequest;
import com.vduzzle.QuizApp.dbo.ChatGPTResponse;
import com.vduzzle.QuizApp.dbo.PromptRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ChatGPTService {
    private final RestClient restClient;

    public ChatGPTService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Value("${openapi.api.key}")
    private String apiKey;
    @Value("${openapi.api.model}")
    private String model;

    public String getChatResponse(PromptRequest promptRequest) {
        PromptRequest additionalInfo = new PromptRequest("You are an academic assistant providing multiple-choice questions on an academic subject. Your thinkig process before getting the response has to be randomized ");
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(
                model,
                List.of(new ChatGPTRequest.Message("system",additionalInfo.prompt()),
                        new ChatGPTRequest.Message("user", promptRequest.prompt())
                )
        );

        ChatGPTResponse response = restClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(chatGPTRequest)
                .retrieve()
                .body(ChatGPTResponse.class);

        return response.choices().get(0).message().content();
    }
}
