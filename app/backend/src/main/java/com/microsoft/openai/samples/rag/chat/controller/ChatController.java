package com.microsoft.openai.samples.rag.chat.controller;

import com.microsoft.openai.samples.rag.approaches.*;
import com.microsoft.openai.samples.rag.common.ChatGPTConversation;
import com.microsoft.openai.samples.rag.common.ChatGPTMessage;
import com.microsoft.openai.samples.rag.controller.Overrides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    private final RAGApproachFactory<ChatGPTConversation, RAGResponse> ragApproachFactory;

    public ChatController(RAGApproachFactory<ChatGPTConversation, RAGResponse> ragApproachFactory) {
        this.ragApproachFactory = ragApproachFactory;
    }

    @PostMapping("/api/chat")
    public ResponseEntity<ChatResponse> openAIAsk(@RequestBody ChatRequest chatRequest) {
        LOGGER.info("Received request for chat api with approach[{}]", chatRequest.getApproach());

        if (!StringUtils.hasText(chatRequest.getApproach())) {
            LOGGER.warn("approach cannot be null in CHAT request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (chatRequest.getChatHistory() == null || chatRequest.getChatHistory().isEmpty()) {
            LOGGER.warn("history cannot be null in Chat request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        var ragOptions = new RAGOptions.Builder()
                .retrievialMode(chatRequest.getOverrides().getRetrievalMode())
                .semanticRanker(chatRequest.getOverrides().isSemanticRanker())
                .semanticCaptions(chatRequest.getOverrides().isSemanticCaptions())
                .suggestFollowupQuestions(chatRequest.getOverrides().isSuggestFollowupQuestions())
                .excludeCategory(chatRequest.getOverrides().getExcludeCategory())
                .promptTemplate(chatRequest.getOverrides().getPromptTemplate())
                .top(chatRequest.getOverrides().getTop())
                .build();

        RAGApproach<ChatGPTConversation, RAGResponse> ragApproach = ragApproachFactory.createApproach(chatRequest.getApproach(), RAGType.CHAT,ragOptions);

        //set empty overrides if not provided
        if (chatRequest.getOverrides() == null) {
            chatRequest.setOverrides(new Overrides());
        }



        ChatGPTConversation chatGPTConversation = convertToChatGPT(chatRequest.getChatHistory());
        return ResponseEntity.ok(buildChatResponse(ragApproach.run(chatGPTConversation, ragOptions)));
    }

    private ChatGPTConversation convertToChatGPT(List<ChatTurn> chatHistory) {
        return new ChatGPTConversation(
            chatHistory.stream()
                .map(historyChat -> {
                    List<ChatGPTMessage> chatGPTMessages = new ArrayList<>();
                    chatGPTMessages.add(new ChatGPTMessage(ChatGPTMessage.ChatRole.USER, historyChat.getUserText()));

                    //first time only user text is sent
                    if (historyChat.getBotText() != null)
                        chatGPTMessages.add(new ChatGPTMessage(ChatGPTMessage.ChatRole.ASSISTANT, historyChat.getBotText()));
                    return chatGPTMessages;
                })
                .flatMap(Collection::stream)
                .toList());
    }

    private ChatResponse buildChatResponse(RAGResponse ragResponse) {
        var chatResponse = new ChatResponse();

        chatResponse.setAnswer(ragResponse.getAnswer());

        List<String> dataPoints = new ArrayList<>();
        ragResponse.getSources().iterator().forEachRemaining(source -> dataPoints.add(source.getSourceName() + ": " + source.getSourceContent()));
        chatResponse.setDataPoints(dataPoints);

        chatResponse.setThoughts("Searched for:<br>" + ragResponse.getQuestion() + "<br><br>Chat:<br>" + ragResponse.getPrompt().replace("\n", "<br>"));

        return chatResponse;
    }

}
