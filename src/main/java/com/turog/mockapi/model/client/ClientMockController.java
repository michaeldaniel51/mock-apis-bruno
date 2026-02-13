package com.turog.mockapi.model.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
public class ClientMockController {

    // In-memory Database for Clients
    private final List<Map<String, Object>> clientDb = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of(
                    "client_id", "CL001",
                    "name", "Seyi Akamo",
                    "type", "Corporate",
                    "status", "Active",
                    "location", "Lagos",
                    "company", "Kijana International",
                    "level", "Tier One",
                    "email", "theemail@example.com",
                    "phone_number", "08162827322",
                    "created_at", "2025-07-01T12:00:00Z"
            ))
    )));

    // 1. List Clients - GET /clients/details
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> listClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // Logic: Filter, then calculate pagination metadata
        List<Map<String, Object>> filtered = clientDb.stream()
                .filter(c -> (search == null || c.get("name").toString().toLowerCase().contains(search.toLowerCase())))
                .filter(c -> (type == null || c.get("type").toString().equalsIgnoreCase(type)))
                .toList();

        int totalRecords = filtered.size();
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);
        int start = (page - 1) * page_size;

        List<Map<String, Object>> pagedClients = filtered.stream()
                .skip(start)
                .limit(page_size)
                .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Clients retrieved successfully.",
                "data", Map.of(
                        "clients", pagedClients,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", totalRecords,
                                "total_pages", totalPages == 0 ? 1 : totalPages
                        )
                )
        ));
    }
    @GetMapping("/details/{client_id}")
    public ResponseEntity<Map<String, Object>> viewClientDetail(@PathVariable String client_id) {

        // 1. Find the basic client info from your database
        Map<String, Object> client = clientDb.stream()
                .filter(c -> c.get("client_id").equals(client_id))
                .findFirst()
                .orElse(null);

        if (client == null) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Client not found"));
        }

        // 2. Build the detailed response using the data from the 'client' object
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Client profile retrieved successfully.",
                "data", Map.of(
                        "profile", Map.of(
                                "client_id", client_id,
                                "name", client.get("name"), // Dynamic name
                                "company", client.getOrDefault("company", "N/A"),
                                "level", client.getOrDefault("level", "Tier One"),
                                "transaction_limit", 500000.00,
                                "daily_limit", 50000.00
                        ),
                        "loan_eligibility", Map.of(
                                "eligible", false,
                                "reason", "User is not eligible for a Loan"
                        ),
                        "loan_compliance", Map.of(
                                "pep_sanctions_status", "Failed",
                                "company_screening_status", "Passed",
                                "aml_screening_status", "Pending"
                        ),
                        "kyc_compliance", Map.of(
                                "address", client.getOrDefault("location", "Lagos, Nigeria"),
                                "dob", "1990-08-29",
                                "phone_number", client.getOrDefault("phone_number", "0000000000"),
                                "email", client.getOrDefault("email", "email@example.com"),
                                "verification_status", "Verified",
                                "id_types", List.of("NIN", "National ID", "Passport"),
                                "verification_docs", List.of(
                                        Map.of("file_name", "doc_verify.pdf", "url", "https://example.com/docs/v1.pdf")
                                ),
                                "utility_docs", List.of(
                                        Map.of("file_name", "utility.pdf", "url", "https://example.com/docs/u1.pdf")
                                )
                        )
                )
        ));
    }

    // 3. Create Client - POST /clients/setup
    @PostMapping("/setup")
    public ResponseEntity<Map<String, Object>> createClient(@RequestBody Map<String, Object> req) {
        // Generate a new ID based on current list size
        String newId = "CL" + String.format("%03d", clientDb.size() + 1);
        String now = ZonedDateTime.now().toString();

        // Prepare the internal record
        Map<String, Object> newClient = new HashMap<>(req);
        newClient.put("client_id", newId);
        newClient.put("status", "Active");
        newClient.put("created_at", now);

        // Save to our mock database
        clientDb.add(newClient);

        // Build the full response data block to match your schema
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("client_id", newId);
        responseData.put("name", req.get("name"));
        responseData.put("company", req.get("company"));
        responseData.put("type", req.get("type"));
        responseData.put("status", "Active");
        responseData.put("created_at", now);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Client created successfully.",
                "data", responseData
        ));
    }

    // 4. Update Client Profile - PUT /clients/profile
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateClientProfile(@RequestBody Map<String, Object> req) {
        String clientId = String.valueOf(req.get("client_id"));
        String updatedAt = ZonedDateTime.now().toString();

        // 1. Find the client in our mock DB and update the fields
        boolean clientFound = false;
        Map<String, Object> updatedFields = new HashMap<>();

        for (Map<String, Object> client : clientDb) {
            if (client.get("client_id").equals(clientId)) {
                // List of fields we allow to be updated
                String[] fieldsToUpdate = {"name", "company", "address", "dob", "phone_number", "email"};

                for (String field : fieldsToUpdate) {
                    if (req.containsKey(field)) {
                        Object newValue = req.get(field);
                        client.put(field, newValue); // Update DB
                        updatedFields.put(field, newValue); // Track for response
                    }
                }
                clientFound = true;
                break;
            }
        }

        if (!clientFound) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Client not found"
            ));
        }

        // 2. Build response matching your exact JSON structure
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Client profile updated successfully.",
                "data", Map.of(
                        "client_id", clientId,
                        "updated_fields", updatedFields,
                        "updated_at", updatedAt
                )
        ));
    }

    // 5. List Client Accounts - GET /clients/accounts (Using PathVariable or Param for ID)
    // Note: Since DELETE/GET usually don't have bodies, the client_id and pagination
    // are typically handled as RequestParams or PathVariables.
    @GetMapping("/accounts/{client_id}")
    public ResponseEntity<Map<String, Object>> listClientsAccount(
            @PathVariable String client_id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // Mock Transaction Data
        List<Map<String, Object>> transactions = List.of(
                Map.of("transaction_id", "TX001", "string", "2025-09-01T10:15:00Z", "amount", 5000.00, "type", "debit", "description", "ATM Withdrawal", "status", "Success"),
                Map.of("transaction_id", "TX002", "string", "2025-08-29T15:30:00Z", "amount", 25000.00, "type", "credit", "description", "Salary Payment", "status", "Success"),
                Map.of("transaction_id", "TX003", "string", "2025-08-28T12:00:00Z", "amount", 3000.00, "type", "debit", "description", "Airtime Purchase", "status", "Success"),
                Map.of("transaction_id", "TX004", "string", "2025-08-27T09:45:00Z", "amount", 1500.00, "type", "debit", "description", "POS Transaction", "status", "Success"),
                Map.of("transaction_id", "TX005", "string", "2025-08-25T18:20:00Z", "amount", 10000.00, "type", "credit", "description", "Transfer from John Doe", "status", "Success")
        );

        // Mock Account Data
        List<Map<String, Object>> accounts = List.of(
                Map.of(
                        "account_id", "AC001",
                        "account_number", "0123456789",
                        "account_type", "Savings",
                        "currency", "NGN",
                        "balance", 150000.75,
                        "transactions", transactions
                ),
                Map.of(
                        "account_id", "AC002",
                        "account_number", "0987654321",
                        "account_type", "Current",
                        "currency", "USD",
                        "balance", 2000.50,
                        "transactions", transactions // Reusing mock list for brevity
                )
        );

        // Response Structure
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Client accounts retrieved successfully.",
                "data", Map.of(
                        "accounts", accounts,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", accounts.size(),
                                "total_pages", 1
                        )
                )
        ));
    }

    // 6. List Client Devices - GET /clients/device
    // 6. List Client Devices - GET /clients/device
    @GetMapping("/device")
    public ResponseEntity<Map<String, Object>> listClientsDevice(@RequestBody Map<String, Object> req) {

        // Extract pagination details from the nested request object
        Map<String, Object> paginationReq = (Map<String, Object>) req.get("pagination");
        int page = (int) paginationReq.getOrDefault("page", 1);
        int pageSize = (int) paginationReq.getOrDefault("page_size", 5);

        // Mock Device Data
        List<Map<String, Object>> devices = List.of(
                Map.of(
                        "device_id", "DV001",
                        "device_type", "Mobile",
                        "os", "iOS 17.0",
                        "ip_address", "192.168.1.10",
                        "last_login", "2025-09-03T14:20:00Z",
                        "status", "Active"
                ),
                Map.of(
                        "device_id", "DV002",
                        "device_type", "Web",
                        "os", "Windows 11",
                        "ip_address", "102.89.45.67",
                        "last_login", "2025-09-01T09:10:00Z",
                        "status", "Inactive"
                )
        );

        // Build the Response
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Client devices retrieved successfully.",
                "data", Map.of(
                        "devices", devices,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", pageSize,
                                "total_records", devices.size(),
                                "total_pages", 1
                        )
                )
        ));
    }

    // 7. Delete Client - DELETE /clients/profile/remove
    @DeleteMapping("/profile/remove")
    public ResponseEntity<Map<String, Object>> deleteClient(@RequestBody Map<String, Object> req) {
        String id = String.valueOf(req.get("client_Id"));
        // Closure requirement: We soft-delete by setting status to 'Inactive' or hard delete from DB
        boolean removed = clientDb.removeIf(c -> c.get("client_id").equals(id));

        return ResponseEntity.ok(Map.of(
                "client_Id", id,
                "deleted", removed,
                "message", removed ? "Client successfully removed" : "Client not found"
        ));
    }
}