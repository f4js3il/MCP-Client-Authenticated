package com.example.demo.controller;


import com.example.demo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatRestController {

    public final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/info")
    public ResponseEntity<String> sendMessage(@RequestParam String question){
        return ResponseEntity.ok(chatService.getChatResponse(question));
    }
}
