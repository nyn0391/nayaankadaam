package com.busgo.bus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buses")
public class BusController {

    private final BusRepository repo;

    public BusController(BusRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Bus> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) { return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }

    @PostMapping
    public Bus create(@RequestBody Bus bus) { if (bus.getId() == null) bus.setId(UUID.randomUUID()); return repo.save(bus); }

    @PutMapping("/{id}")
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
    public ResponseEntity<?> delete(@PathVariable UUID id) { repo.deleteById(id); return ResponseEntity.ok().build(); }
}
