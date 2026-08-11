package com.busgo.layout;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.busgo.security.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seat-layouts")
public class SeatLayoutController {

    private final SeatLayoutRepository repo;
    private final SecurityUtils securityUtils;

    public SeatLayoutController(SeatLayoutRepository repo, SecurityUtils securityUtils) { this.repo = repo; this.securityUtils = securityUtils; }

    @GetMapping
    public List<SeatLayout> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SeatLayout layout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!securityUtils.isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "forbidden"));
        if (layout.getId() == null) layout.setId(UUID.randomUUID());
        var uid = securityUtils.getUserId(auth);
        if (uid != null) layout.setCreatedBy(uid);
        return ResponseEntity.ok(repo.save(layout));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody SeatLayout in) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!securityUtils.isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "forbidden"));
        return repo.findById(id).map(existing -> {
            existing.setName(in.getName());
            existing.setDescription(in.getDescription());
            existing.setFormat(in.getFormat());
            existing.setContent(in.getContent());
            existing.setMeta(in.getMeta());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) { Authentication auth = SecurityContextHolder.getContext().getAuthentication(); if (!securityUtils.isAdmin(auth)) return ResponseEntity.status(403).body(Map.of("success", false, "message", "forbidden")); repo.deleteById(id); return ResponseEntity.ok().build(); }
}
