package com.turog.mockapi.model.cards;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cards")
@CrossOrigin(origins = "*")
public class CardMockController {

    // In-memory Database for Cards
    private final List<Map<String, Object>> cardDb = new CopyOnWriteArrayList<>(List.of(
            new HashMap<>(Map.of(
                    "card_id", "CD001",
                    "client_id", "CL001",
                    "card_type", "Virtual",
                    "masked_number", "****1234",
                    "expiry_date", "2027-08",
                    "status", "Active",
                    "currency", "NGN",
                    "limit", 500000.0,
                    "created_at", "2025-07-01T10:00:00Z"
            ))
    ));

    // 1. List Client Cards - GET /cards/internal?client_id=CL001&page=1&page_size=10
    @GetMapping("/internal")
    public ResponseEntity<Map<String, Object>> listClientCards(
            @RequestParam(required = false) String client_id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // 1. Filter by client_id if provided
        List<Map<String, Object>> filteredCards = cardDb.stream()
                .filter(c -> client_id == null || String.valueOf(c.get("client_id")).equals(client_id))
                .toList();

        int totalRecords = filteredCards.size();

        // 2. Calculate pagination logic
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);
        int start = (page - 1) * page_size;

        // 3. Apply Pagination (Skip/Limit based on page)
        List<Map<String, Object>> pagedCards = filteredCards.stream()
                .skip(start)
                .limit(page_size)
                .toList();

        // 4. Return the response with page-based metadata
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cards retrieved successfully.",
                "data", Map.of(
                        "cards", pagedCards,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", totalRecords,
                                "total_pages", totalPages == 0 ? 1 : totalPages
                        )
                )
        ));
    }

    // 2. Create Card - POST /cards/internal
    @PostMapping("/internal")
    public ResponseEntity<Map<String, Object>> createCard(@RequestBody Map<String, Object> request) {
        String newId = "CD" + String.format("%03d", cardDb.size() + 1);
        String maskedNum = "****" + (1000 + new Random().nextInt(9000));

        Map<String, Object> newCard = new HashMap<>();
        newCard.put("card_id", newId);
        newCard.put("client_id", request.get("client_id"));
        newCard.put("card_type", request.get("card_type"));
        newCard.put("currency", request.get("currency"));
        newCard.put("limit", request.get("limit"));
        newCard.put("masked_number", maskedNum);
        newCard.put("expiry_date", "2028-01");
        newCard.put("status", "Active");
        newCard.put("created_at", ZonedDateTime.now().toString());

        cardDb.add(newCard);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Card created successfully.",
                "data", Map.of(
                        "card_id", newId,
                        "client_id", request.get("client_id"),
                        "card_type", request.get("card_type"),
                        "masked_number", maskedNum,
                        "expiry_date", "2028-01",
                        "status", "Active"
                )
        ));
    }

    // 3. Card Details - GET /cards/internal/{card_id}
    @GetMapping("/internal/{card_id}")
    public ResponseEntity<Map<String, Object>> cardDetails(@PathVariable String card_id) {
        return cardDb.stream()
                .filter(c -> c.get("card_id").equals(card_id))
                .findFirst()
                .map(c -> ResponseEntity.ok(Map.of("status", "success", "message", "Card details retrieved successfully.", "data", c)))
                .orElse(ResponseEntity.status(404).body(Map.of("status", "error", "message", "Card not found")));
    }

    // 4. Update Card Status - PUT /cards/internal/status
    @PutMapping("/internal/status")
    public ResponseEntity<Map<String, Object>> updateCardStatus(@RequestBody Map<String, Object> request) {
        String cardId = String.valueOf(request.get("card_id"));
        String newStatus = String.valueOf(request.get("status"));

        for (Map<String, Object> card : cardDb) {
            if (card.get("card_id").equals(cardId)) {
                card.put("status", newStatus);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Card status updated successfully.",
                        "data", Map.of("card_id", cardId, "status", newStatus, "updated_at", ZonedDateTime.now().toString())
                ));
            }
        }
        return ResponseEntity.status(404).build();
    }

    // 5. Reset Card Pin - PUT /cards/pin/reset
    @PutMapping("/pin/reset")
    public ResponseEntity<Map<String, Object>> resetCardPin(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Card PIN reset successfully.",
                "data", Map.of(
                        "card_id", request.get("card_id"),
                        "pin_reset", true,
                        "reset_at", ZonedDateTime.now().toString()
                )
        ));
    }

    // 6. Block Card - PUT /cards/internal/block
    @PutMapping("/internal/block")
    public ResponseEntity<Map<String, Object>> blockCard(@RequestBody Map<String, Object> request) {
        String cardId = String.valueOf(request.get("card_id"));
        String reason = String.valueOf(request.get("reason"));

        for (Map<String, Object> card : cardDb) {
            if (card.get("card_id").equals(cardId)) {
                card.put("status", "Blocked");
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Card blocked successfully.",
                        "data", Map.of(
                                "card_id", cardId,
                                "status", "Blocked",
                                "blocked_at", ZonedDateTime.now().toString(),
                                "reason", reason
                        )
                ));
            }
        }
        return ResponseEntity.status(404).build();
    }
}