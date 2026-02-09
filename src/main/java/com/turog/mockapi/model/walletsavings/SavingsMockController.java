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

    // 1. Get Savings Accounts
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> getSavingsAccounts() {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Accounts retrieved successfully.",
                "data", Map.of("accounts", accountsDb, "pagination", Map.of("page", 1, "page_size", 10, "total_records", accountsDb.size(), "total_pages", 1))));
    }

    // 2. Search Saving Accounts
    @GetMapping("/accounts/search")
    public ResponseEntity<Map<String, Object>> searchSavingAccounts(@RequestParam String query) {
        List<Map<String, Object>> results = accountsDb.stream()
                .filter(a -> a.get("customer_name").toString().contains(query) || a.get("account_number").toString().contains(query))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Search completed.", "data", Map.of("accounts", results)));
    }

    // 3. Get Details
    @GetMapping("/accounts/{account_id}")
    public ResponseEntity<Map<String, Object>> getDetails(@PathVariable String account_id) {
        return accountsDb.stream().filter(a -> a.get("account_id").equals(account_id)).findFirst()
                .map(a -> ResponseEntity.ok(Map.of("status", "success", "message", "Account details retrieved.", "data", a)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Get Performance
    @GetMapping("/accounts/performance/{account_id}")
    public ResponseEntity<Map<String, Object>> getPerformance(@PathVariable String account_id) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Performance metrics retrieved.",
                "data", Map.of("account_id", account_id, "interest_accrued", 120.50, "growth_rate", 3.2, "last_interest_posting", "2025-08-30T10:00:00Z")));
    }

    // 5. Get Overview
    @GetMapping("/accounts/overview/{account_id}")
    public ResponseEntity<Map<String, Object>> getOverview(@PathVariable String account_id) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Overview retrieved.",
                "data", Map.of("account_id", account_id, "balance", 5000.75, "last_transaction", Map.of("id", "TXN1001", "type", "Deposit", "amount", 1000.0, "string", "2025-09-01T14:00:00Z"))));
    }

    // 6. Get Transactions
    @GetMapping("/accounts/transactions/{account_id}")
    public ResponseEntity<Map<String, Object>> getTransactions(@PathVariable String account_id) {
        List<Map<String, Object>> filtered = transactionsDb.stream().filter(t -> t.get("account_id").equals(account_id)).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Transactions retrieved.",
                "data", Map.of("transactions", filtered, "pagination", Map.of("page", 1, "page_size", 5, "total_records", filtered.size(), "total_pages", 1))));
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

    // 10. Change Status
    @PutMapping("/accounts/status/{account_id}")
    public ResponseEntity<Map<String, Object>> changeStatus(@PathVariable String account_id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Account status updated.",
                "data", Map.of("account_id", account_id, "old_status", "Active", "new_status", req.get("status"), "updated_at", ZonedDateTime.now().toString())));
    }

    // 11. Close Account
    @PostMapping("/accounts/close")
    public ResponseEntity<Map<String, Object>> closeAccount(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Account closed successfully.",
                "data", Map.of("account_id", req.get("account_id"), "closed_at", ZonedDateTime.now().toString(), "final_balance", 6000.75, "transferred_to", req.get("destination_account"))));
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

    // 14. Archive Account
    @PutMapping("/accounts/archive")
    public ResponseEntity<Map<String, Object>> archive(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("status", "success", "message", "Account archived successfully.",
                "data", Map.of("account_id", req.get("account_id"), "archived_at", ZonedDateTime.now().toString())));
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