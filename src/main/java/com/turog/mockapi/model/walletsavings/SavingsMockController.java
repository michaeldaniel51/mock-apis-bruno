package com.turog.mockapi.model.walletsavings;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/savings")
@CrossOrigin(origins = "*")
public class SavingsMockController {

    // In-memory Database for Accounts
    private final List<Map<String, Object>> accountsDb = new CopyOnWriteArrayList<>(Arrays.asList(
            new HashMap<>(Map.of("account_id", "SAV001", "account_number", "1234567890", "customer_name", "John Doe", "balance", 5000.75, "status", "Active", "currency", "NGN", "opened_at", "2025-01-15T10:00:00Z"))
    ));

    // In-memory Database for Transactions
    private final List<Map<String, Object>> transactionsDb = new CopyOnWriteArrayList<>();

    // 1. Get Savings Accounts - GET /accounts?page=1&page_size=10
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getSavingsAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {
        int start = (page - 1) * page_size;
        List<Map<String, Object>> paged = accountsDb.stream().skip(start).limit(page_size).toList();
        int totalRecords = accountsDb.size();
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);

        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of(
                "accounts", paged,
                "pagination", Map.of("page", page, "page_size", page_size, "total_records", totalRecords, "total_pages", totalPages == 0 ? 1 : totalPages)
        )));
    }

    // 2. Search Saving Accounts
    @GetMapping("/accounts/search")
    public ResponseEntity<Map<String, Object>> searchSavingAccounts(@RequestParam String query) {
        List<Map<String, Object>> results = accountsDb.stream()
                .filter(a -> a.get("customer_name").toString().contains(query) || a.get("account_number").toString().contains(query))
                .toList();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Search completed.", "data", Map.of("accounts", results)));
    }

    // 3. Get Details
    @GetMapping("/accounts/{account_id}")
    public ResponseEntity<Map<String, Object>> getDetails(@PathVariable String account_id) {
        return accountsDb.stream().filter(a -> a.get("account_id").equals(account_id)).findFirst()
                .map(a -> ResponseEntity.ok(Map.of("status", "success", "message", "Account details retrieved.", "data", a)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Get Performance - Linked to actual Balance
    @GetMapping("/accounts/performance/{account_id}")
    public ResponseEntity<Map<String, Object>> getPerformance(@PathVariable String account_id) {
        return accountsDb.stream().filter(a -> a.get("account_id").equals(account_id)).findFirst()
                .map(a -> {
                    double balance = (double) a.get("balance");
                    return ResponseEntity.ok(Map.of("status", "success", "data", Map.of(
                            "account_id", account_id,
                            "interest_accrued", balance * 0.02, // 2% mock interest
                            "growth_rate", 3.2,
                            "last_interest_posting", ZonedDateTime.now().minusDays(1).toString()
                    )));
                }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Get Overview - Fetches real balance and real last transaction
    @GetMapping("/accounts/overview/{account_id}")
    public ResponseEntity<Map<String, Object>> getOverview(@PathVariable String account_id) {
        return accountsDb.stream().filter(a -> a.get("account_id").equals(account_id)).findFirst()
                .map(a -> {
                    // Find actual last transaction for this account
                    Map<String, Object> lastTxn = transactionsDb.stream()
                            .filter(t -> t.get("account_id").equals(account_id))
                            .reduce((first, second) -> second) // Get the last one
                            .orElse(Map.of("message", "No transactions yet"));

                    return ResponseEntity.ok(Map.of("status", "success", "data", Map.of(
                            "account_id", account_id,
                            "balance", a.get("balance"),
                            "status", a.get("status"),
                            "last_transaction", lastTxn
                    )));
                }).orElse(ResponseEntity.notFound().build());
    }
    // 6. Get Transactions (Paginated)
    @GetMapping("/accounts/transactions/{account_id}")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @PathVariable String account_id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int page_size) {

        List<Map<String, Object>> filtered = transactionsDb.stream()
                .filter(t -> t.get("account_id").equals(account_id))
                .toList();

        int start = (page - 1) * page_size;
        List<Map<String, Object>> paged = filtered.stream().skip(start).limit(page_size).toList();

        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of(
                "transactions", paged,
                "pagination", Map.of("page", page, "page_size", page_size, "total_records", filtered.size(), "total_pages", (int) Math.ceil((double) filtered.size() / page_size))
        )));
    }

    // 7. Deposit
    @PostMapping("/accounts/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@RequestBody Map<String, Object> req) {
        return processFinancial(req, "Deposit", true);
    }

    // 8. Withdraw
    @PostMapping("/accounts/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody Map<String, Object> req) {
        return processFinancial(req, "Withdrawal", false);
    }

    // 9. Assign Staff
    @PutMapping("/accounts/assign-staff/{account_id}")
    public ResponseEntity<Map<String, Object>> assignStaff(@PathVariable String account_id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Staff assigned successfully.",
                "data", Map.of("account_id", account_id, "assigned_staff", req.get("staff_id"))));
    }

    // 10. Change Status - Updates the actual DB
    @PutMapping("/accounts/status/{account_id}")
    public ResponseEntity<Map<String, Object>> changeStatus(@PathVariable String account_id, @RequestBody Map<String, Object> req) {
        for (Map<String, Object> acc : accountsDb) {
            if (acc.get("account_id").equals(account_id)) {
                String oldStatus = String.valueOf(acc.get("status"));
                acc.put("status", req.get("status"));
                return ResponseEntity.ok(Map.of("status", "success", "message", "Status updated.",
                        "data", Map.of("account_id", account_id, "old_status", oldStatus, "new_status", acc.get("status"))));
            }
        }
        return ResponseEntity.notFound().build();
    }



    @PostMapping("/accounts/close")
    public ResponseEntity<Map<String, Object>> closeAccount(@RequestBody Map<String, Object> req) {
        String sourceId = String.valueOf(req.get("account_id"));
        String destinationId = String.valueOf(req.get("destination_account"));

        Map<String, Object> sourceAcc = null;
        Map<String, Object> destAcc = null;

        // 1. Locate both accounts in the DB
        for (Map<String, Object> acc : accountsDb) {
            if (acc.get("account_id").equals(sourceId)) sourceAcc = acc;
            if (acc.get("account_id").equals(destinationId)) destAcc = acc;
        }

        // 2. Validation
        if (sourceAcc == null) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Source account not found."));
        }
        if (destAcc == null) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", "Destination account not found."));
        }

        // 3. Perform the Transfer
        double transferAmount = (double) sourceAcc.get("balance");
        double currentDestBalance = (double) destAcc.get("balance");

        // Update Source
        sourceAcc.put("balance", 0.0);
        sourceAcc.put("status", "Closed");

        // Update Destination
        destAcc.put("balance", currentDestBalance + transferAmount);

        // 4. Log the transaction
        Map<String, Object> closeTxn = new HashMap<>(Map.of(
                "transaction_id", "TXN-CLOSE-" + System.currentTimeMillis(),
                "source_account", sourceId,
                "destination_account", destinationId,
                "amount", transferAmount,
                "type", "Closure Transfer",
                "timestamp", ZonedDateTime.now().toString()
        ));
        transactionsDb.add(closeTxn);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Account closed and balance transferred successfully.",
                "data", Map.of(
                        "closed_account", sourceId,
                        "received_account", destinationId,
                        "transferred_amount", transferAmount,
                        "new_destination_balance", destAcc.get("balance"),
                        "closed_at", ZonedDateTime.now().toString()
                )
        ));
    }

    // 12. Add Charge
    @PostMapping("/accounts/charge")
    public ResponseEntity<Map<String, Object>> addCharge(@RequestBody Map<String, Object> req) {
        return processFinancial(req, String.valueOf(req.get("charge_type")), false);
    }

    // 13. Transfer Funds
    @PostMapping("/accounts/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody Map<String, Object> req) {
        return processFinancial(req, "Transfer", false);
    }

    // 14. Archive Account - PUT /savings/accounts/archive
    @PutMapping("/accounts/archive")
    public ResponseEntity<Map<String, Object>> archive(@RequestBody Map<String, Object> req) {
        String accId = String.valueOf(req.get("account_id"));

        for (Map<String, Object> acc : accountsDb) {
            if (acc.get("account_id").equals(accId)) {
                acc.put("is_archived", true);
                acc.put("archived_at", ZonedDateTime.now().toString());

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Account archived successfully.",
                        "data", Map.of(
                                "account_id", accId,
                                "is_archived", true,
                                "archived_at", acc.get("archived_at")
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Account not found."));
    }

    // 15. Unarchive Account - PUT /savings/accounts/unarchive
    @PutMapping("/accounts/unarchive")
    public ResponseEntity<Map<String, Object>> unarchive(@RequestBody Map<String, Object> req) {
        String accId = String.valueOf(req.get("account_id"));

        for (Map<String, Object> acc : accountsDb) {
            if (acc.get("account_id").equals(accId)) {
                acc.put("is_archived", false);
                acc.remove("archived_at"); // Clean up the timestamp

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Account unarchived successfully.",
                        "data", Map.of(
                                "account_id", accId,
                                "is_archived", false,
                                "unarchived_at", ZonedDateTime.now().toString()
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Account not found."));
    }

    // Helper to handle balance math and txn logs
    private ResponseEntity<Map<String, Object>> processFinancial(Map<String, Object> req, String type, boolean isCredit) {
        String accId = String.valueOf(req.get("account_id"));
        double amount = Double.parseDouble(req.get("amount").toString());
        for (Map<String, Object> acc : accountsDb) {
            if (acc.get("account_id").equals(accId)) {
                double newBal = (double) acc.get("balance") + (isCredit ? amount : -amount);
                acc.put("balance", newBal);
                String tid = "TXN" + (transactionsDb.size() + 500);
                Map<String, Object> txn = new HashMap<>(Map.of("transaction_id", tid, "account_id", accId, "type", type, "amount", amount, "string", ZonedDateTime.now().toString(), "status", "Success"));
                transactionsDb.add(txn);
                return ResponseEntity.ok(Map.of("status", "success", "message", type + " successful.",
                        "data", Map.of("transaction_id", tid, "account_id", accId, "amount", amount, "new_balance", newBal, "string", ZonedDateTime.now().toString())));
            }
        }
        return ResponseEntity.notFound().build();
    }
}