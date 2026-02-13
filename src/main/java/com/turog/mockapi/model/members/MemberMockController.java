package com.turog.mockapi.model.members;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/members")
@CrossOrigin(origins = "*")
public class MemberMockController {

    private final List<Map<String, Object>> memberDatabase = new CopyOnWriteArrayList<>(Arrays.asList(
            new HashMap<>(Map.of("member_id", "M001", "name", "John Doe", "role", "Admin", "location", "Lagos", "status", "Active")),
            new HashMap<>(Map.of("member_id", "M002", "name", "Jane Smith", "role", "User", "location", "Abuja", "status", "Inactive"))
    ));
    // Create Member - POST /members/profile
    @PostMapping("/profile")
    public ResponseEntity<Map<String, Object>> createMemberProfile(@RequestBody Map<String, Object> request) {
        String newId = "M00" + (memberDatabase.size() + 1);
        String fullName = request.getOrDefault("firstName", "") + " " + request.getOrDefault("lastName", "");

        // Add to our "database"
        Map<String, Object> newMember = new HashMap<>();
        newMember.put("member_id", newId);
        newMember.put("name", fullName.trim());
        newMember.put("location", request.getOrDefault("location", "Unknown"));
        newMember.put("role", request.getOrDefault("role", "User"));
        newMember.put("status", "Active");
        memberDatabase.add(newMember);

        // Prepare Response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("data", Map.of(
                "userId", newId,
                "firstName", request.get("firstName"),
                "lastName", request.get("lastName"),
                "email", request.get("email"),
                "createdAt", ZonedDateTime.now().toString(),
                "isVerified", false
        ));
        response.put("meta", Map.of("timestamp", ZonedDateTime.now().toString(), "apiVersion", "v1"));

        return ResponseEntity.status(201).body(response);
    }
    // 2. Member List - GET /members/profile?page=1&page_size=10
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> listMember(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        // 1. Calculate the starting index
        int start = (page - 1) * page_size;

        // 2. Slice the member database
        List<Map<String, Object>> pagedMembers = memberDatabase.stream()
                .skip(start)
                .limit(page_size)
                .toList();

        // 3. Calculate metadata
        int totalRecords = memberDatabase.size();
        int totalPages = (int) Math.ceil((double) totalRecords / page_size);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Members retrieved successfully.",
                "data", Map.of(
                        "members", pagedMembers,
                        "pagination", Map.of(
                                "page", page,
                                "page_size", page_size,
                                "total_records", totalRecords,
                                "total_pages", totalPages == 0 ? 1 : totalPages
                        )
                )
        ));
    }

    // 3. View Member - GET /members/profile/{member_id}
    @GetMapping("/profile/{member_id}")
    public ResponseEntity<Map<String, Object>> viewMember(@PathVariable String member_id) {
        Map<String, Object> member = memberDatabase.stream()
                .filter(m -> m.get("member_id").equals(member_id))
                .findFirst()
                .orElse(null);

        if (member == null) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Member not found"));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Member retrieved successfully.",
                "data", member
        ));
    }

    // 4. Update Member Details - PUT /members/profile/{member_id}
    @PutMapping("/profile/{member_id}")
    public ResponseEntity<Map<String, Object>> updateMemberDetails(@PathVariable String member_id, @RequestBody Map<String, Object> request) {
        for (Map<String, Object> member : memberDatabase) {
            if (member.get("member_id").equals(member_id)) {
                // Update the fields allowed in your schema
                if (request.containsKey("phone_number")) member.put("phone_number", request.get("phone_number"));
                if (request.containsKey("email")) member.put("email", request.get("email"));

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Member information updated successfully.",
                        "data", Map.of(
                                "member_id", member_id,
                                "phone_number", member.getOrDefault("phone_number", "+2348012345678"),
                                "email", member.getOrDefault("email", "johndoe@example.com"),
                                "updated_at", "2025-09-04T10:30:00Z"
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Member not found"));
    }

    // 5. Change Password - PUT /members/password
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Password changed successfully.",
                "data", Map.of(
                        "member_id", request.getOrDefault("member_id", "M001"),
                        "password_changed_at", "2025-09-04T14:20:00Z"
                )
        ));
    }

    // 6. Upload Profile Image - POST /members/avatar
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Profile image uploaded successfully.",
                "data", Map.of(
                        "member_id", request.getOrDefault("member_id", "M001"),
                        "profile_image_url", "https://example.com/images/members/M001.jpg",
                        "uploaded_at", "2025-09-04T16:45:00Z"
                )
        ));
    }

    // 7. Remove Member - DELETE /members/profile/remove
    @DeleteMapping("/profile/remove")
    public ResponseEntity<Map<String, Object>> removeMember(@RequestBody Map<String, Object> request) {
        String idToRemove = String.valueOf(request.get("memberId"));
        boolean removed = memberDatabase.removeIf(m -> m.get("member_id").equals(idToRemove));

        return ResponseEntity.ok(Map.of(
                "memberId", idToRemove,
                "removed", removed,
                "message", removed ? "Member removed successfully" : "Member not found"
        ));
    }
}