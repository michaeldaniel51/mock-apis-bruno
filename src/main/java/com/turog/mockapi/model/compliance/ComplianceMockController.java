package com.turog.mockapi.model.compliance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/compliance")
@CrossOrigin(origins = "*")
public class ComplianceMockController {

    // 1. Run AML Check (All) - POST /compliance/checks/run-aml
    private final List<Map<String, Object>> complianceDatabase = new CopyOnWriteArrayList<>();

    // 1. Run AML Check (All) - POST /compliance/checks/run-aml
    @PostMapping("/checks/run-aml")
    public ResponseEntity<Map<String, Object>> runAmlCheck(@RequestBody Map<String, Object> request) {
        String checkId = "AML" + (complianceDatabase.size() + 101);

        // Create the record
        Map<String, Object> newCheck = new HashMap<>();
        newCheck.put("check_Id", checkId);
        newCheck.put("entityId", request.getOrDefault("entityId", "UNKNOWN"));
        newCheck.put("status", "completed");

        // Matching your results schema
        Map<String, Object> results = new HashMap<>();
        results.put("pep", false);
        results.put("sanctions", false);
        results.put("adverseMedia", false);
        results.put("companyStatus", "active");

        newCheck.put("results", results);

        // Save to our "database"
        complianceDatabase.add(newCheck);

        return ResponseEntity.ok(newCheck);
    }

    // 2. Re-run AML Check - POST /compliance/checks/rerun-aml/{check_id}
    @PostMapping("/checks/rerun-aml/{check_id}")
    public ResponseEntity<Map<String, Object>> rerunAmlCheck(@PathVariable String check_id) {
        // Find existing check
        Map<String, Object> existing = complianceDatabase.stream()
                .filter(c -> String.valueOf(c.get("check_Id")).equals(check_id))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Check ID not found"));
        }

        // Return the schema for a re-run
        Map<String, Object> response = new HashMap<>(existing);
        response.put("rerun", true);
        response.put("status", "completed");

        return ResponseEntity.ok(response);
    }

    // 3. Run Eligibility Checks - POST /compliance/checks/loan-eligibility
    @PostMapping("/checks/loan-eligibility")
    public ResponseEntity<Map<String, Object>> runEligibilityChecks(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        response.put("eligibilityId", "ELG" + (System.currentTimeMillis() % 10000));
        response.put("client_Id", request.getOrDefault("client_Id", "CL123"));
        response.put("status", "eligible");

        response.put("checks", Map.of(
                "aml", "passed",
                "pep", "passed",
                "sanctions", "passed",
                "creditScore", 720,
                "adverseMedia", "passed"
        ));

        return ResponseEntity.ok(response);
    }

}