package com.turog.mockapi.model.loans;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "*")
public class LoanMockController {

    private final List<Map<String, Object>> loanApplicationsDb = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> activeLoansDb = new CopyOnWriteArrayList<>(new ArrayList<>(List.of(
            new HashMap<>(Map.of("loan_Id", "LN001", "client_Id", "CL123", "amount", 10000.0, "status", "active", "balance", 7500.0, "repaymentTerm", "12 months"))
    )));
    private final List<Map<String, Object>> guarantorsDb = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> loanTransactionsDb = new CopyOnWriteArrayList<>();

    // 1. Loan Accounts - GET /loans/account
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getLoanAccounts() {
        return ResponseEntity.ok(Map.of("loans", activeLoansDb));
    }

    // 2. Update Loan Account - PUT /loans/account/{loan_id}
    @PutMapping("/account/{loan_id}")
    public ResponseEntity<Map<String, Object>> updateLoanAccount(@PathVariable String loan_id, @RequestBody Map<String, Object> req) {
        activeLoansDb.stream().filter(l -> l.get("loan_Id").equals(loan_id)).findFirst()
                .ifPresent(l -> { l.put("amount", req.get("amount")); l.put("repaymentTerm", req.get("repaymentTerm")); });
        return ResponseEntity.ok(Map.of("loan_Id", loan_id, "updated", true));
    }

    // 3. Loan Account Details - GET /loans/account/{loan_id}
    @GetMapping("/account/{loan_id}")
    public ResponseEntity<Map<String, Object>> getLoanDetails(@PathVariable String loan_id) {
        return activeLoansDb.stream().filter(l -> l.get("loan_Id").equals(loan_id)).findFirst()
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 4. Loan Repayment Schedule - GET /loans/account/schedule/{loan_id}
    @GetMapping("/account/schedule/{loan_id}")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable String loan_id) {
        return ResponseEntity.ok(Map.of("loan_Id", loan_id, "schedule", List.of(
                Map.of("installment", 1, "dueDate", "2025-10-01", "amount", 500, "status", "paid"),
                Map.of("installment", 2, "dueDate", "2025-11-01", "amount", 500, "status", "pending")
        )));
    }

    // 5. Loan Account Transactions - GET /loans/account/transactions/{loan_id}
    @GetMapping("/account/transactions/{loan_id}")
    public ResponseEntity<Map<String, Object>> getLoanTransactions(@PathVariable String loan_id) {
        return ResponseEntity.ok(Map.of("transactions", List.of(Map.of("transaction_Id", "TXN123", "type", "repayment", "amount", 500, "string", "2025-08-01"))));
    }

    // 6. Close Loan Account - DELETE /loans/account/close/{loan_id}
    @DeleteMapping("/account/close/{loan_id}")
    public ResponseEntity<Map<String, Object>> closeLoan(@PathVariable String loan_id) {
        activeLoansDb.removeIf(l -> l.get("loan_Id").equals(loan_id));
        return ResponseEntity.ok(Map.of("loan_Id", loan_id, "closed", true));
    }

    // 7. Add Loan Guarantor - POST /loans/account/guarantors
    @PostMapping("/account/guarantors")
    public ResponseEntity<Map<String, Object>> addGuarantor(@RequestBody Map<String, Object> req) {
        guarantorsDb.add(new HashMap<>(req));
        return ResponseEntity.ok(Map.of("loan_Id", req.get("loan_Id"), "guarantor_Id", req.get("guarantor_Id"), "added", true));
    }

    // 8. Remove Loan Guarantor - DELETE /loans/account/guarantors/{guarantor_id}/{loan_id}
    @DeleteMapping("/account/guarantors/{guarantor_id}/{loan_id}")
    public ResponseEntity<Map<String, Object>> removeGuarantor(@PathVariable String guarantor_id, @PathVariable String loan_id) {
        guarantorsDb.removeIf(g -> g.get("guarantor_Id").equals(guarantor_id) && g.get("loan_Id").equals(loan_id));
        return ResponseEntity.ok(Map.of("loan_Id", loan_id, "guarantor_Id", guarantor_id, "removed", true));
    }

    // 9. List Loan Guarantor - GET /loans/account/guarantors-list/{loan_id}
    @GetMapping("/account/guarantors-list/{loan_id}")
    public ResponseEntity<Map<String, Object>> listGuarantors(@PathVariable String loan_id) {
        List<Map<String, Object>> filtered = guarantorsDb.stream().filter(g -> g.get("loan_Id").equals(loan_id)).toList();
        return ResponseEntity.ok(Map.of("guarantors", filtered));
    }

    // 10. Update Loan Status - PUT /loans/account/status
    @PutMapping("/account/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody Map<String, Object> req) {
        activeLoansDb.stream().filter(l -> l.get("loan_Id").equals(req.get("loan_Id"))).findFirst().ifPresent(l -> l.put("status", req.get("status")));
        return ResponseEntity.ok(Map.of("loan_Id", req.get("loan_Id"), "status", req.get("status"), "updated", true));
    }

    // 11. Loan Charges - POST /loans/account/charges
    @PostMapping("/account/charges")
    public ResponseEntity<Map<String, Object>> addCharge(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("loan_Id", req.get("loan_Id"), "chargeId", "CH" + System.currentTimeMillis() % 1000, "added", true));
    }

    // 12. Submit Loan Application - POST /loans/applications
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> submitApp(@RequestBody Map<String, Object> req) {
        String appId = "APP" + (loanApplicationsDb.size() + 1);
        Map<String, Object> app = new HashMap<>(req); app.put("application_Id", appId); app.put("status", "pending");
        loanApplicationsDb.add(app);
        return ResponseEntity.ok(Map.of("application_Id", appId, "submitted", true));
    }

    // 13. List Loan Applications - GET /loan/applications
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> listApps() {
        return ResponseEntity.ok(Map.of("applications", loanApplicationsDb));
    }

    // 14. Review Loan Application - PUT /loan/applications/review
    @PutMapping("/applications/review")
    public ResponseEntity<Map<String, Object>> reviewApp(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("application_Id", req.get("application_Id"), "decision", req.get("decision"), "reviewed", true));
    }

    // 15. Approve Loan - POST /loans/applications/approve
    @PostMapping("/applications/approve")
    public ResponseEntity<Map<String, Object>> approveLoan(@RequestBody Map<String, Object> req) {
        String loanId = "LN" + (activeLoansDb.size() + 101);
        return ResponseEntity.ok(Map.of("application_Id", req.get("application_Id"), "approved", true, "loan_Id", loanId));
    }

    // 16. Reject Loan - POST /loan/applications/reject
    @PostMapping("/applications/reject")
    public ResponseEntity<Map<String, Object>> rejectLoan(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("application_Id", req.get("application_Id"), "rejected", true));
    }

    // 17. Disburse Loan - POST /loans/application/disburse
    @PostMapping("/application/disburse")
    public ResponseEntity<Map<String, Object>> disburseLoan(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Map.of("loan_Id", req.get("loan_Id"), "disbursed", true, "transaction_Id", "TXN" + System.currentTimeMillis() % 1000));
    }
}