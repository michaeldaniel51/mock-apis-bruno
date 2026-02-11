package com.turog.mockapi.model.documents;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/documents")
public class DocumentMockController {

    // In-memory Document Metadata Database
    private final List<Map<String, Object>> documentDb = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of(
                    "document_id", "DOC001",
                    "name", "ID Card",
                    "type", "PDF",
                    "size", 245678,
                    "status", "Active",
                    "created_at", "2025-09-05T13:00:00Z"
            )),
            new HashMap<>(Map.of(
                    "document_id", "DOC002",
                    "name", "Proof of Address",
                    "type", "Image",
                    "size", 124578,
                    "status", "Active",
                    "created_at", "2025-09-01T11:00:00Z"
            ))
    )));

    // 1. List Documents - GET /documents/generic
    @GetMapping("/generic")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Documents retrieved successfully.",
                "data", Map.of(
                        "documents", documentDb,
                        "pagination", Map.of("page", 1, "page_size", 10, "total_records", documentDb.size(), "total_pages", 1)
                )
        ));
    }

    // 2. Preview Image Thumbnail - GET /documents/image/preview/{document_id}
    @GetMapping("/image/preview/{document_id}")
    public ResponseEntity<Map<String, Object>> previewDocument(@PathVariable String document_id) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Document preview generated successfully.",
                "data", Map.of(
                        "document_id", document_id,
                        "preview_url", "https://files.example.com/preview/" + document_id,
                        "expires_at", ZonedDateTime.now().plusHours(1).toString()
                )
        ));
    }

    // 3. Download Document - GET /documents/generic/download/{document_id}
    @GetMapping("/generic/download/{document_id}")
    public ResponseEntity<Map<String, Object>> downloadDocument(@PathVariable String document_id) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Document download link generated.",
                "data", Map.of(
                        "document_id", document_id,
                        "download_url", "https://files.example.com/download/" + document_id,
                        "expires_at", ZonedDateTime.now().plusHours(1).toString()
                )
        ));
    }

    // 4. Upload Document - POST /documents/generic
    @PostMapping("/generic")
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestBody Map<String, Object> req) {
        String newId = "DOC" + String.format("%03d", documentDb.size() + 1);

        Map<String, Object> newDoc = new HashMap<>();
        newDoc.put("document_id", newId);
        newDoc.put("name", req.get("name"));
        newDoc.put("type", req.get("type"));
        newDoc.put("size", 534567); // Mocked size
        newDoc.put("status", "Active");
        newDoc.put("created_at", ZonedDateTime.now().toString());

        documentDb.add(newDoc);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Document uploaded successfully.",
                "data", Map.of(
                        "document_id", newId,
                        "name", req.get("name"),
                        "type", req.get("type"),
                        "size", 534567,
                        "status", "Active",
                        "uploaded_at", ZonedDateTime.now().toString()
                )
        ));
    }

    // 5. Update Document - PUT /documents/generic
    @PutMapping("/generic")
    public ResponseEntity<Map<String, Object>> updateDocument(@RequestBody Map<String, Object> req) {
        String docId = String.valueOf(req.get("document_id"));

        for (Map<String, Object> doc : documentDb) {
            if (doc.get("document_id").equals(docId)) {
                if (req.containsKey("name")) doc.put("name", req.get("name"));

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Document updated successfully.",
                        "data", Map.of(
                                "document_id", docId,
                                "updated_fields", Map.of("name", req.get("name")),
                                "updated_at", ZonedDateTime.now().toString()
                        )
                ));
            }
        }
        return ResponseEntity.status(404).build();
    }

    // 6. Delete Document - DELETE /documents/generic/{document_id}
    @DeleteMapping("/generic/{document_id}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable String document_id) {
        boolean removed = documentDb.removeIf(d -> d.get("document_id").equals(document_id));

        if (removed) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Document deleted successfully.",
                    "data", Map.of("document_id", document_id, "deleted_at", ZonedDateTime.now().toString())
            ));
        }
        return ResponseEntity.status(404).build();
    }
}