package com.turog.mockapi.model.connections;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/connections")
public class ConnectionsMockController {

    // Dynamic Database for Connections
    private final List<Map<String, Object>> connectionDb = new CopyOnWriteArrayList<>(Arrays.asList(
            new HashMap<>(Map.of(
                    "connection_id", "CON001",
                    "name", "ERP Adapter",
                    "type", "ERP",
                    "status", "Active",
                    "config", Map.of(
                            "encryption_key", "encKey123",
                            "public_key", "pubKey123",
                            "private_key", "privKey123",
                            "identifier", "erp-001"
                    ),
                    "created_at", "2025-07-10T12:00:00Z"
            ))
    ));

    // 1. List Connections - GET /connections/applications
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> listConnections() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Connections retrieved successfully.",
                "data", Map.of(
                        "connections", connectionDb,
                        "pagination", Map.of("page", 1, "page_size", 10, "total_records", connectionDb.size(), "total_pages", 1)
                )
        ));
    }

    // 2. Create Connection - POST /connections/applications
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> createConnection(@RequestBody Map<String, Object> request) {
        String newId = "CON" + String.format("%03d", connectionDb.size() + 1);

        Map<String, Object> newConn = new HashMap<>(request);
        newConn.put("connection_id", newId);
        newConn.put("status", "Active");
        newConn.put("created_at", ZonedDateTime.now().toString());

        connectionDb.add(newConn);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Connection created successfully.",
                "data", newConn
        ));
    }

    // 3. View Connection Detail - GET /connections/applications/{connection_id}
    @GetMapping("/applications/{connection_id}")
    public ResponseEntity<Map<String, Object>> viewConnectionDetail(@PathVariable String connection_id) {
        return connectionDb.stream()
                .filter(c -> c.get("connection_id").equals(connection_id))
                .findFirst()
                .map(c -> ResponseEntity.ok(Map.of("status", "success", "message", "Connection details retrieved.", "data", c)))
                .orElse(ResponseEntity.status(404).body(Map.of("status", "error", "message", "Connection not found")));
    }

    // 4. Edit Connection - PUT /connections/applications
    @PutMapping("/applications")
    public ResponseEntity<Map<String, Object>> editConnection(@RequestBody Map<String, Object> request) {
        String id = String.valueOf(request.get("connection_id"));

        for (Map<String, Object> conn : connectionDb) {
            if (conn.get("connection_id").equals(id)) {
                Map<String, Object> updatedFields = new HashMap<>();
                if (request.containsKey("name")) {
                    conn.put("name", request.get("name"));
                    updatedFields.put("name", request.get("name"));
                }
                if (request.containsKey("config")) conn.put("config", request.get("config"));

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Connection updated successfully.",
                        "data", Map.of("connection_id", id, "updated_fields", updatedFields, "updated_at", ZonedDateTime.now().toString())
                ));
            }
        }
        return ResponseEntity.status(404).build();
    }

    // 5. Pause Connection - PUT /connections/applications/pause
    @PutMapping("/applications/pause")
    public ResponseEntity<Map<String, Object>> pauseConnection(@RequestBody Map<String, Object> request) {
        String id = String.valueOf(request.get("connection_id"));
        connectionDb.stream().filter(c -> c.get("connection_id").equals(id)).findFirst().ifPresent(c -> c.put("status", "Paused"));

        return ResponseEntity.ok(Map.of("status", "success", "message", "Connection paused successfully.",
                "data", Map.of("connection_id", id, "status", "Paused", "updated_at", ZonedDateTime.now().toString())));
    }

    // 6. Start Connection - PUT /connections/applications/activate
    @PutMapping("/applications/activate")
    public ResponseEntity<Map<String, Object>> startConnection(@RequestBody Map<String, Object> request) {
        String id = String.valueOf(request.get("connection_id"));
        connectionDb.stream().filter(c -> c.get("connection_id").equals(id)).findFirst().ifPresent(c -> c.put("status", "Active"));

        return ResponseEntity.ok(Map.of("status", "success", "message", "Connection started successfully.",
                "data", Map.of("connection_id", id, "status", "Active", "updated_at", ZonedDateTime.now().toString())));
    }

    // 7. Test Connection - POST /connections/applications/test
    @PostMapping("/applications/test")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Connection test completed.",
                "data", Map.of("connection_id", request.get("connection_id"), "test_result", "Passed", "tested_at", ZonedDateTime.now().toString())));
    }

    // 8. Delete Connection - DELETE /connections/applications/{connectionId}
    @DeleteMapping("/applications/{connectionId}")
    public ResponseEntity<Map<String, Object>> deleteConnection(@PathVariable String connectionId) {
        boolean removed = connectionDb.removeIf(c -> c.get("connection_id").equals(connectionId));
        return ResponseEntity.ok(Map.of(
                "connectionId", connectionId,
                "deleted", removed,
                "message", removed ? "Connection successfully deleted" : "Connection not found"
        ));
    }
}