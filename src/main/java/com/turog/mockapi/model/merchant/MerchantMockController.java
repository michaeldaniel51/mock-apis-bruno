package com.turog.mockapi.model.merchant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/merchants")
@CrossOrigin(origins = "*")
public class MerchantMockController {

    // In-memory "Database" for Merchants
    private final List<Map<String, Object>> merchantDb = new CopyOnWriteArrayList<>(Arrays.asList(
            new HashMap<>(Map.of("merchant_id", "MC001", "name", "Termii", "type", "SMS", "status", "Active", "created_at", "2025-07-15T10:00:00Z")),
            new HashMap<>(Map.of("merchant_id", "MC002", "name", "ZeptoMail", "type", "Email", "status", "Active", "created_at", "2025-06-20T14:30:00Z"))
    ));

    // 1. List Merchants - GET /merchants/subscribed/list?page=1&page_size=10
    @GetMapping("/subscribed/list")
    public ResponseEntity<Map<String, Object>> listMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // 1. Calculate the starting index
        int start = (page - 1) * page_size;

        // 2. Slice the merchant database
        List<Map<String, Object>> pagedMerchants = merchantDb.stream()
                .skip(start)
                .limit(page_size)
                .toList();

        // 3. Calculate pagination metadata
        int totalRecords = merchantDb.size();
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Merchants retrieved successfully.",
                "data", Map.of(
                        "merchants", pagedMerchants,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", totalRecords,
                                "total_pages", totalPages == 0 ? 1 : totalPages
                        )
                )
        ));
    }

    // 2. View Merchant - GET /merchants/subscribed/{merchant_id}
    @GetMapping("/subscribed/{merchant_id}")
    public ResponseEntity<Map<String, Object>> viewMerchant(@PathVariable String merchant_id) {
        return merchantDb.stream()
                .filter(m -> m.get("merchant_id").equals(merchant_id))
                .findFirst()
                .map(m -> ResponseEntity.ok(Map.of("status", "success", "message", "Merchant retrieved successfully.", "data", m)))
                .orElse(ResponseEntity.status(404).body(Map.of("status", "error", "message", "Merchant not found")));
    }

    // 3. Add New Merchant - POST /merchants/setup
    @PostMapping("/setup")
    public ResponseEntity<Map<String, Object>> addNewMerchant(@RequestBody Map<String, Object> request) {
        String newId = "MC" + String.format("%03d", merchantDb.size() + 1);
        String now = ZonedDateTime.now().toString();

        Map<String, Object> newMerchant = new HashMap<>(request);
        newMerchant.put("merchant_id", newId);
        newMerchant.put("created_at", now);

        merchantDb.add(newMerchant);

        return ResponseEntity.status(201).body(Map.of(
                "status", "success",
                "message", "Merchant added successfully.",
                "data", newMerchant
        ));
    }

    // 4. Update Merchant Configuration - PUT /merchants/setup
    @PutMapping("/setup")
    public ResponseEntity<Map<String, Object>> updateMerchant(@RequestBody Map<String, Object> request) {
        String merchantId = String.valueOf(request.get("merchant_id"));

        for (Map<String, Object> merchant : merchantDb) {
            if (merchant.get("merchant_id").equals(merchantId)) {
                // Update specific fields from request
                merchant.putAll(request);
                merchant.put("updated_at", ZonedDateTime.now().toString());

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Merchant configuration updated successfully.",
                        "data", merchant
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Merchant not found"));
    }

    // 5. Remove Merchant - DELETE /merchants/setup
    @DeleteMapping("/setup")
    public ResponseEntity<Map<String, Object>> removeMerchant(@RequestBody Map<String, Object> request) {
        String merchantId = String.valueOf(request.get("merchant_id"));
        boolean removed = merchantDb.removeIf(m -> m.get("merchant_id").equals(merchantId));

        if (removed) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Merchant deleted successfully.",
                    "data", Map.of("merchant_id", merchantId, "deleted_at", ZonedDateTime.now().toString())
            ));
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Merchant not found"));
    }
}