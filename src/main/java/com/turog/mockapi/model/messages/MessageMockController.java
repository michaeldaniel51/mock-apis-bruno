package com.turog.mockapi.model.messages;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages/internal")
@CrossOrigin(origins = "*")
public class MessageMockController {

    // In-memory Mailbox Database
    private final List<Map<String, Object>> messageDb = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of(
                    "messageId", "MSG001",
                    "subject", "Welcome!",
                    "body", "Thank you for joining our platform.",
                    "sender", "system",
                    "recipients", List.of("user123"),
                    "attachments", List.of("welcome_guide.pdf"), // Example attachment
                    "category", "inbox",
                    "timestamp", "2025-09-03T10:00:00Z",
                    "status", "unread"
            ))
    )));

    // 1. List Messages by Category - GET /messages/internal/category?type=inbox&page=1&page_size=10
//    @GetMapping("/category")
//    public ResponseEntity<Map<String, Object>> listMessages(
//            @RequestParam(defaultValue = "inbox") String type,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int page_size) {
//
//        // 1. Filter by category and map to summary (removing body)
//        List<Map<String, Object>> filtered = messageDb.stream()
//                .filter(m -> String.valueOf(m.get("category")).equalsIgnoreCase(type))
//                .map(m -> {
//                    Map<String, Object> summary = new HashMap<>(m);
//                    summary.remove("body");
//                    return summary;
//                })
//                .collect(Collectors.toList());
//
//        // 2. Calculate pagination logic
//        int totalRecords = filtered.size();
//        int totalPages = (int) Math.ceil((double) totalRecords / page_size);
//        int start = (page - 1) * page_size;
//
//        // 3. Slice the filtered list
//        List<Map<String, Object>> pagedMessages = filtered.stream()
//                .skip(Math.max(0, start))
//                .limit(page_size)
//                .toList();
//
//        return ResponseEntity.ok(Map.of(
//                "status", "success",
//                "message", "Messages retrieved successfully.",
//                "data", Map.of(
//                        "messages", pagedMessages,
//                        "pagination", Map.of(
//                                "page", page,
//                                "page_size", page_size,
//                                "total_records", totalRecords,
//                                "total_pages", totalPages == 0 ? 1 : totalPages
//                        )
//                )
//        ));
//    }

    // 1. List Messages by Category - GET /messages/internal/category?type=inbox&page=1&page_size=10
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> listMessages(
            @RequestParam(defaultValue = "inbox") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // 1. Filter by category & convert to Summary (removing body for list view)
        List<Map<String, Object>> filtered = messageDb.stream()
                .filter(m -> String.valueOf(m.get("category")).equalsIgnoreCase(type))
                .map(m -> {
                    Map<String, Object> summary = new HashMap<>(m);
                    summary.remove("body");

                    // Ensure recipients and attachments are consistently Lists even in summary
                    summary.putIfAbsent("recipients", List.of());
                    summary.putIfAbsent("attachments", List.of());

                    return summary;
                })
                .toList();

        // 2. Pagination Math
        int totalRecords = filtered.size();
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);
        int start = (page - 1) * page_size;

        // 3. Slice the list safely
        List<Map<String, Object>> pagedMessages = filtered.stream()
                .skip(Math.max(0, start))
                .limit(page_size)
                .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Messages retrieved successfully.",
                "data", Map.of(
                        "messages", pagedMessages,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", totalRecords,
                                "total_pages", totalPages == 0 ? 1 : totalPages
                        )
                )
        ));
    }

    // 2. Message Details - GET /messages/internal/{message_id}
    @GetMapping("/{message_id}")
    public ResponseEntity<Map<String, Object>> getMessageDetails(@PathVariable String message_id) {
        return messageDb.stream()
                .filter(m -> String.valueOf(m.get("messageId")).equals(message_id))
                .findFirst()
                .map(m -> {
                    // Defensive check: Ensure attachments is never null in response
                    Map<String, Object> response = new HashMap<>(m);
                    response.putIfAbsent("attachments", List.of());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Send Message - POST /messages/internal
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> req) {
        String newId = "MSG" + String.format("%03d", messageDb.size() + 1);

        Map<String, Object> newMessage = new HashMap<>(req);

        // Normalize recipients: Ensure it's a list even if one string is sent
        Object recipients = req.get("recipients");
        if (recipients instanceof String) {
            newMessage.put("recipients", List.of(recipients));
        }

        // Handle Attachments: Default to empty list if null
        if (req.get("attachments") == null) {
            newMessage.put("attachments", List.of());
        }

        newMessage.put("messageId", newId);
        newMessage.put("timestamp", ZonedDateTime.now().toString());
        newMessage.put("status", "unread");
        newMessage.put("category", "sent");

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