package com.turog.mockapi.model.messages;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages/internal")
public class MessageMockController {

    // In-memory Mailbox Database
    private final List<Map<String, Object>> messageDb = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of(
                    "messageId", "MSG001",
                    "subject", "Welcome!",
                    "body", "Thank you for joining our platform.",
                    "sender", "system",
                    "recipient", "user123",
                    "category", "inbox",
                    "timestamp", "2025-09-03T10:00:00Z",
                    "status", "unread"
            ))
    )));

    // 1. List Messages by Category - GET /messages/internal/category
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> listMessages(@RequestParam(defaultValue = "inbox") String type) {
        List<Map<String, Object>> filtered = messageDb.stream()
                .filter(m -> String.valueOf(m.get("category")).equalsIgnoreCase(type))
                .map(m -> {
                    // Return summary view (no body)
                    Map<String, Object> summary = new HashMap<>(m);
                    summary.remove("body");
                    return summary;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("messages", filtered));
    }

    // 2. Message Details - GET /messages/internal/{message_id}
    @GetMapping("/{message_id}")
    public ResponseEntity<Map<String, Object>> getMessageDetails(@PathVariable String message_id) {
        return messageDb.stream()
                .filter(m -> m.get("messageId").equals(message_id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Send Message - POST /messages/internal
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> req) {
        String newId = "MSG" + String.format("%03d", messageDb.size() + 1);

        Map<String, Object> newMessage = new HashMap<>(req);
        newMessage.put("messageId", newId);
        newMessage.put("timestamp", ZonedDateTime.now().toString());
        newMessage.put("status", "unread");
        newMessage.put("category", "sent"); // Mocking as sent category for the sender

        messageDb.add(newMessage);

        return ResponseEntity.ok(Map.of("messageId", newId, "sent", true));
    }

    // 4. Mark As Read - PUT /messages/internal/read/{message_id}
    @PutMapping("/read/{message_id}")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String message_id) {
        messageDb.stream()
                .filter(m -> m.get("messageId").equals(message_id))
                .findFirst()
                .ifPresent(m -> m.put("status", "read"));

        return ResponseEntity.ok(Map.of("messageId", message_id, "status", "read"));
    }

    // 5. Mark As Unread - PUT /messages/internal/unread/{message_id}
    @PutMapping("/unread/{message_id}")
    public ResponseEntity<Map<String, Object>> markAsUnread(@PathVariable String message_id) {
        messageDb.stream()
                .filter(m -> m.get("messageId").equals(message_id))
                .findFirst()
                .ifPresent(m -> m.put("status", "unread"));

        return ResponseEntity.ok(Map.of("messageId", message_id, "status", "unread"));
    }

    // 6. Delete Message - DELETE /messages/internal/{message_id}
    @DeleteMapping("/{message_id}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable String message_id) {
        boolean removed = messageDb.removeIf(m -> m.get("messageId").equals(message_id));
        return ResponseEntity.ok(Map.of("messageId", message_id, "deleted", removed));
    }
}