package com.example.demo.service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolCallbacks;


    @Value("classpath:/templates/prompt.st")
    private Resource template;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       List<McpSyncClient> clients) {
        this.chatClient = chatClientBuilder.build();
        this.mcpToolCallbacks = SyncMcpToolCallbackProvider.builder().mcpClients(clients).build();
    }

    public String getChatResponse(String question){
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Message userMessage = promptTemplate.createMessage(Map.of("question",question));
         ChatResponse response=chatClient
                 .prompt(new Prompt(userMessage))
                 .toolCallbacks(mcpToolCallbacks)
                 .call()
                 .chatResponse();



        return response.getResult().getOutput().getText();

    }
}
