package com.busgo.admin;

import com.busgo.admin.dto.RoleAssignRequest;
import com.busgo.user.Role;
import com.busgo.user.RoleRepository;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class RoleController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public RoleController(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listRoles() {
        List<Role> roles = roleRepo.findAll();
        List<Map<String, Object>> out = roles.stream().map(r -> Map.of("id", r.getId(), "name", r.getName(), "description", r.getDescription())).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", out));
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable UUID userId, @RequestBody RoleAssignRequest req) {
        Optional<User> ou = userRepo.findById(userId);
        if (ou.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found"));
        Optional<Role> or = roleRepo.findByName(req.getRoleName());
        if (or.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "Role not found"));
        User user = ou.get();
        Role role = or.get();
        user.addRole(role);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Role assigned"));
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> revokeRole(@PathVariable UUID userId, @PathVariable String roleName) {
        Optional<User> ou = userRepo.findById(userId);
        if (ou.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found"));
        Optional<Role> or = roleRepo.findByName(roleName);
        if (or.isEmpty()) return ResponseEntity.status(404).body(Map.of("success", false, "message", "Role not found"));
        User user = ou.get();
        Role role = or.get();
        user.removeRole(role);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Role revoked"));
    }
}
