package com.busgo.bus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

import com.busgo.security.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buses")
public class BusController {

    private final BusRepository repo;
    private final SecurityUtils securityUtils;

    public BusController(BusRepository repo, SecurityUtils securityUtils) { this.repo = repo; this.securityUtils = securityUtils; }

    @GetMapping
    public List<Bus> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) { return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Bus bus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (bus.getId() == null) bus.setId(UUID.randomUUID());
        var uid = securityUtils.getUserId(auth);
        if (uid != null) bus.setCreatedBy(uid);
        return ResponseEntity.ok(repo.save(bus));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Bus in) {
        return repo.findById(id).map(existing -> {
            existing.setModel(in.getModel());
            existing.setRegistrationNumber(in.getRegistrationNumber());
            existing.setSeatLayoutId(in.getSeatLayoutId());
            existing.setTotalSeats(in.getTotalSeats());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) { repo.deleteById(id); return ResponseEntity.ok().build(); }
}
