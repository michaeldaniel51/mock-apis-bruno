package com.turog.mockapi.model.communities;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/communities")
@CrossOrigin(origins = "*")
public class CommunityMockController {


    // Dynamic data stores
    private final List<Map<String, Object>> communityDatabase = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> inviteDatabase = new CopyOnWriteArrayList<>();

    // 1. Add New Community - POST /communities/account
    @PostMapping("/account")
    public ResponseEntity<Map<String, Object>> addNewCommunity(@RequestBody Map<String, Object> request) {
        String communityId = "COM" + String.format("%03d", communityDatabase.size() + 1);

        Map<String, Object> newCommunity = new HashMap<>();
        newCommunity.put("community_id", communityId);
        newCommunity.put("name", request.get("name"));
        newCommunity.put("description", request.get("description"));
        newCommunity.put("category", request.get("category"));
        newCommunity.put("status", "Active");
        newCommunity.put("created_at", ZonedDateTime.now().toString());
       // newCommunity.put("members", new ArrayList<Map<String, String>>()); // Initialize empty member list

        communityDatabase.add(newCommunity);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Community created successfully.",
                "data", newCommunity
        ));
    }

    // 2. Communities List - GET /communities/accounts
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> communitiesList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", communityDatabase,
                "pagination", Map.of("page", page, "page_size", page_size, "total_records", communityDatabase.size())
        ));
    }

    // 3. Search Community - GET /communities/accounts/search
    @GetMapping("/accounts/search")
    public ResponseEntity<Map<String, Object>> searchCommunity(@RequestParam String name) {
        List<Map<String, Object>> results = communityDatabase.stream()
                .filter(c -> String.valueOf(c.get("name")).toLowerCase().contains(name.toLowerCase()))
                .toList();

        return ResponseEntity.ok(Map.of("status", "success", "data", results));
    }

    // 4. View Community Detail - GET /communities/account/{community_id}
    @GetMapping("/account/{community_id}")
    public ResponseEntity<Map<String, Object>> viewCommunityDetail(@PathVariable String community_id) {
        return communityDatabase.stream()
                .filter(c -> c.get("community_id").equals(community_id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Community not found")));
    }

    // 5. Update Community Detail - PUT /communities/account/{community_id}
    @PutMapping("/account/{community_id}")
    public ResponseEntity<Map<String, Object>> updateCommunityDetail(
            @PathVariable String community_id, @RequestBody Map<String, Object> request) {

        for (Map<String, Object> community : communityDatabase) {
            if (community.get("community_id").equals(community_id)) {

                // This map will store only the fields that were actually updated
                Map<String, Object> updatedFields = new HashMap<>();

                // Logic to update fields and track them for the response
                String[] fieldsToUpdate = {"name", "description", "category"};
                for (String field : fieldsToUpdate) {
                    if (request.containsKey(field)) {
                        Object newValue = request.get(field);
                        community.put(field, newValue);
                        updatedFields.put(field, newValue);
                    }
                }

                // Constructing the exact response schema you shared
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Community updated successfully.",
                        "data", Map.of(
                                "community_id", community_id,
                                "updated_fields", updatedFields, // strictly matching {"field": "value"}
                                "updated_at", "2025-09-03T13:00:00Z" // or ZonedDateTime.now().toString()
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Community not found"));
    }

    // 6. Invite To Community - POST /communities/account/invites
    @PostMapping("/account/invites")
    public ResponseEntity<Map<String, Object>> inviteToCommunity(@RequestBody Map<String, Object> request) {
        String inviteId = "INV" + String.format("%03d", inviteDatabase.size() + 1);
        List<Map<String, Object>> invites = (List<Map<String, Object>>) request.get("invites");

        List<Map<String, Object>> createdInvites = new ArrayList<>();
        for (Map<String, Object> inviteReq : invites) {
            Map<String, Object> newInvite = new HashMap<>();
            newInvite.put("invite_id", inviteId);
            newInvite.put("community_id", request.get("community_id"));
            newInvite.put("email", inviteReq.get("email"));
            newInvite.put("role", inviteReq.get("role"));
            newInvite.put("status", "Pending");
            inviteDatabase.add(newInvite);
            createdInvites.add(newInvite);
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Invites sent successfully.",
                "data", Map.of("invites", createdInvites)
        ));
    }

    // 7. Accept Invite - POST /communities/invites/accept
    @PostMapping("/invites/accept")
    public ResponseEntity<Map<String, Object>> acceptInvite(@RequestBody Map<String, Object> request) {
        String inviteId = String.valueOf(request.get("invite_id"));

        // Simulating the user joining a community
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Invite accepted.",
                "data", Map.of(
                        "community_id", "COM001",
                        "member_id", "MBR" + (System.currentTimeMillis() % 1000),
                        "role", "Member",
                        "joined_at", ZonedDateTime.now().toString()
                )
        ));
    }

    // 8. Delete Community - DELETE /communities/profile/remove/{community_id}
    @DeleteMapping("/profile/remove/{community_id}")
    public ResponseEntity<Map<String, Object>> deleteCommunity(@PathVariable String community_id) {
        boolean removed = communityDatabase.removeIf(c -> c.get("community_id").equals(community_id));
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", removed ? "Community deleted successfully" : "Community not found"
        ));
    }

    // 9. Dissolve Community - PUT /communities/account/update/{community_id}
    @PutMapping("/account/update/{community_id}")
    public ResponseEntity<Map<String, Object>> dissolveCommunity(@PathVariable String community_id) {
        for (Map<String, Object> community : communityDatabase) {
            if (community.get("community_id").equals(community_id)) {

                // Update the state in our "DB"
                community.put("status", "Dissolved");
                String dissolvedAt = "2025-09-03T14:00:00Z"; // Static as per your sample or ZonedDateTime.now()

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Community dissolved successfully.",
                        "data", Map.of(
                                "community_id", community_id,
                                "dissolved_at", dissolvedAt
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Community not found"));
    }

    // 10. Block / Unblock Community - PUT /communities/account/status/{community_id}
    @PutMapping("/account/status/{community_id}")
    public ResponseEntity<Map<String, Object>> blockUnblockCommunity(
            @PathVariable String community_id,
            @RequestBody Map<String, Object> request) {

        for (Map<String, Object> community : communityDatabase) {
            if (community.get("community_id").equals(community_id)) {

                String oldStatus = String.valueOf(community.get("status"));
                String newStatus = String.valueOf(request.get("status"));

                // Update the status in our memory
                community.put("status", newStatus);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Community status updated.",
                        "data", Map.of(
                                "community_id", community_id,
                                "old_status", oldStatus,
                                "new_status", newStatus,
                                "updated_at", "2025-09-03T14:30:00Z"
                        )
                ));
            }
        }
        return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Community not found"));
    }

    // 11. Get Pending Invites - POST /communities/account/invites/pending
// Changed from GET to POST to support a Request Body
    @GetMapping("/account/invites/pending")
    public ResponseEntity<Map<String, Object>> getPendingInvites(@RequestBody Map<String, Object> request) {

        String communityId = String.valueOf(request.get("community_id"));

        // Filter the invite database for pending invites belonging to this community
        List<Map<String, Object>> pendingInvites = inviteDatabase.stream()
                .filter(invite -> String.valueOf(invite.get("community_id")).equals(communityId))
                .filter(invite -> "Pending".equalsIgnoreCase(String.valueOf(invite.get("status"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Pending invites retrieved successfully.",
                "data", Map.of(
                        "community_id", communityId,
                        "invites", pendingInvites
                )
        ));
    }

    // 12. Cancel Invite - DELETE /communities/account/invites/{invite_id}
    @DeleteMapping("/account/invites/{invite_id}")
    public ResponseEntity<Map<String, Object>> cancelInvite(
            @PathVariable String invite_id,
            @RequestBody Map<String, Object> request) {

        boolean removed = inviteDatabase.removeIf(i -> i.get("invite_id").equals(invite_id));

        return ResponseEntity.ok(Map.of(
                "community_id", request.get("community_id"),
                "invite_id", invite_id,
                "status", removed ? "Cancelled" : "Not Found"
        ));
    }

    // 13. Decline Invite - POST /communities/account/invites/decline
    @PostMapping("/account/invites/decline")
    public ResponseEntity<Map<String, Object>> declineInvite(@RequestBody Map<String, Object> request) {
        String inviteId = String.valueOf(request.get("invite_id"));

        // Update status in database
        inviteDatabase.stream()
                .filter(i -> i.get("invite_id").equals(inviteId))
                .findFirst()
                .ifPresent(i -> i.put("status", "Declined"));

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Invite declined.",
                "data", Map.of(
                        "invite_id", inviteId,
                        "declined_at", "2025-09-03T15:20:00Z"
                )
        ));
    }

    // 14. View Member List - POST /communities/account/members
// Using POST with body as per your requirement
    @PostMapping("/account/members")
    public ResponseEntity<Map<String, Object>> viewMemberList(@RequestBody Map<String, Object> request) {
        String communityId = String.valueOf(request.get("community_id"));

        // Find community and return its members
        Optional<Map<String, Object>> community = communityDatabase.stream()
                .filter(c -> c.get("community_id").equals(communityId))
                .findFirst();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", community.map(c -> c.get("members")).orElse(new ArrayList<>())
        ));
    }

    // 15. Modify Community Member Role - PUT /communities/members/role/
    @PutMapping("/members/role")
    public ResponseEntity<Map<String, Object>> modifyComMemberRole(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Member role updated.",
                "data", Map.of(
                        "member_id", request.get("member_id"),
                        "old_role", "Member",
                        "new_role", request.get("new_role"),
                        "updated_at", "2025-09-03T15:45:00Z"
                )
        ));
    }

    // 16. Delete Community - DELETE /communities/profile/remove
    @DeleteMapping("/profile/remove")
    public ResponseEntity<Map<String, Object>> deleteCommunity(@RequestBody Map<String, Object> request) {
        String communityId = String.valueOf(request.get("community_id"));

        // Attempt to remove the community from our in-memory list
        boolean removed = communityDatabase.removeIf(c ->
                String.valueOf(c.get("community_id")).equals(communityId)
        );

        if (removed) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Community deleted successfully.",
                    "data", Map.of(
                            "community_id", communityId,
                            "deactivated_at", "2025-09-03T16:00:00Z" // Simulated timestamp
                    )
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Community not found or already deleted."
            ));
        }
    }

}
