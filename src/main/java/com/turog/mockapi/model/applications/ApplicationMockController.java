package com.turog.mockapi.model.applications;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/applications")
public class ApplicationMockController {

    // 1. Global Application Catalog
    private final List<Map<String, Object>> globalAppsDb = new CopyOnWriteArrayList<>(Arrays.asList(
            new HashMap<>(Map.of("application_id", "APP001", "name", "CRM Pro", "category", "CRM", "type", "Cloud", "provider", "Provider A", "description", "Customer management tool", "created_at", "2025-07-12T14:00:00Z")),
            new HashMap<>(Map.of("application_id", "APP002", "name", "ERP Lite", "category", "ERP", "type", "Cloud", "provider", "Provider B", "description", "Enterprise resource planning", "created_at", "2025-06-10T10:00:00Z"))
    ));

    // 2. Client-Specific Application Subscriptions
    private final List<Map<String, Object>> clientAppRegistry = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of("client_id", "CL001", "application_id", "APP001", "status", "Active", "settings", Map.of("theme", "light")))
    )));

    // 1. List Applications - GET /applications/clients
    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> listApplications() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Applications retrieved successfully.",
                "data", Map.of(
                        "applications", globalAppsDb,
                        "pagination", Map.of("page", 1, "page_size", 10, "total_records", globalAppsDb.size(), "total_pages", 1)
                )
        ));
    }

    // 2. Search Application - GET /applications/clients/search
    @GetMapping("/clients/search")
    public ResponseEntity<Map<String, Object>> searchApplications(@RequestParam String query) {
        List<Map<String, Object>> results = globalAppsDb.stream()
                .filter(app -> app.get("name").toString().toLowerCase().contains(query.toLowerCase()) ||
                        app.get("category").toString().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "message", "Search completed successfully.", "data", Map.of("applications", results)));
    }

    // 3. Filter Application - GET /applications/clients/filter
    @GetMapping("/clients/filter")
    public ResponseEntity<Map<String, Object>> filterApplication(@RequestParam String category, @RequestParam String type) {
        List<Map<String, Object>> filtered = globalAppsDb.stream()
                .filter(app -> app.get("category").equals(category) && app.get("type").equals(type))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "message", "Applications filtered successfully.", "data", Map.of("applications", filtered)));
    }

    // 4. View Application Details - GET /applications/clients/{application_id}
    @GetMapping("/clients/{application_id}")
    public ResponseEntity<Map<String, Object>> viewApplicationDetails(@PathVariable String application_id) {
        return globalAppsDb.stream()
                .filter(app -> app.get("application_id").equals(application_id))
                .findFirst()
                .map(app -> ResponseEntity.ok(Map.of("status", "success", "message", "Application details retrieved.", "data", app)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Add Application To Plan - POST /applications/clients/plans
    @PostMapping("/clients/plans")
    public ResponseEntity<Map<String, Object>> addApplicationToPlan(@RequestBody Map<String, Object> req) {
        clientAppRegistry.add(new HashMap<>(Map.of(
                "client_id", "CL001", // Defaulted for mock context
                "application_id", req.get("application_id"),
                "plan_id", req.get("plan_id"),
                "status", "Active"
        )));
        return ResponseEntity.ok(Map.of("status", "success", "message", "Application added to plan successfully.",
                "data", Map.of("plan_id", req.get("plan_id"), "application_id", req.get("application_id"), "added_at", ZonedDateTime.now().toString())));
    }

    // 6. List My Applications - GET /applications/clients/{client_id}
    // Fixed path to /clients/{client_id} to match your GET requirement
    @GetMapping("/clients/my/{client_id}")
    public ResponseEntity<Map<String, Object>> listMyApplications(@PathVariable String client_id) {
        List<String> myAppIds = clientAppRegistry.stream()
                .filter(reg -> reg.get("client_id").equals(client_id))
                .map(reg -> reg.get("application_id").toString())
                .toList();

        List<Map<String, Object>> myApps = globalAppsDb.stream()
                .filter(app -> myAppIds.contains(app.get("application_id")))
                .map(app -> {
                    Map<String, Object> map = new HashMap<>(app);
                    map.put("status", "Active");
                    return map;
                }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "message", "Client applications retrieved.", "data", Map.of("applications", myApps)));
    }

    // 7. Edit My Application - PUT /applications/clients/{application_id}
    @PutMapping("/clients/{application_id}")
    public ResponseEntity<Map<String, Object>> editMyApplication(@PathVariable String application_id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Application updated successfully.",
                "data", Map.of("application_id", application_id, "updated_fields", req.get("settings"), "updated_at", ZonedDateTime.now().toString())));
    }

    @DeleteMapping("/clients/{application_id}")
    public ResponseEntity<Map<String, Object>> removeMyApplication(@PathVariable String application_id) {

        // Find the record first to capture the client_id for the response data
        Optional<Map<String, Object>> targetEntry = clientAppRegistry.stream()
                .filter(reg -> reg.get("application_id").equals(application_id))
                .findFirst();

        if (targetEntry.isPresent()) {
            String clientId = targetEntry.get().get("client_id").toString();

            // Perform the removal
            clientAppRegistry.removeIf(reg -> reg.get("application_id").equals(application_id));

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Application removed successfully.",
                    "data", Map.of(
                            "client_id", clientId,
                            "application_id", application_id,
                            "removed_at", ZonedDateTime.now().toString()
                    )
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Application not found in your registry."
            ));
        }
    }

}