package com.busgo.route;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteRepository repo;

    public RouteController(RouteRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Route> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) { return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }

    @PostMapping
    public Route create(@RequestBody Route route) { if (route.getId() == null) route.setId(UUID.randomUUID()); return repo.save(route); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Route in) {
        return repo.findById(id).map(existing -> {
            existing.setCode(in.getCode());
            existing.setOrigin(in.getOrigin());
            existing.setDestination(in.getDestination());
            existing.setStops(in.getStops());
            existing.setDistanceKm(in.getDistanceKm());
            existing.setDurationMinutes(in.getDurationMinutes());
            repo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) { repo.deleteById(id); return ResponseEntity.ok().build(); }
}
