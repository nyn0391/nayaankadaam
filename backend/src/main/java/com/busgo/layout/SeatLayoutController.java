package com.busgo.layout;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seat-layouts")
public class SeatLayoutController {

    private final SeatLayoutRepository repo;

    public SeatLayoutController(SeatLayoutRepository repo) { this.repo = repo; }

    @GetMapping
    public List<SeatLayout> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public SeatLayout create(@RequestBody SeatLayout layout) {
        if (layout.getId() == null) layout.setId(UUID.randomUUID());
        return repo.save(layout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody SeatLayout in) {
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
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
