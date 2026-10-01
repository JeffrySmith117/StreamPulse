package com.streampulse.backend.simulator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/simulator")
@ConditionalOnProperty(name = "streampulse.simulator.enabled", havingValue = "true")
public class SimulatorController {

    private final PlaybackEventSimulator simulator;

    public SimulatorController(PlaybackEventSimulator simulator) {
        this.simulator = simulator;
    }

    @GetMapping("/regions/degraded")
    public Set<String> degradedRegions() {
        return simulator.getDegradedRegions();
    }

    @PostMapping("/regions/{region}/degrade")
    public ResponseEntity<Void> degrade(@PathVariable String region) {
        simulator.degrade(validRegion(region));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/regions/{region}/degrade")
    public ResponseEntity<Void> restore(@PathVariable String region) {
        simulator.restore(validRegion(region));
        return ResponseEntity.noContent().build();
    }

    private String validRegion(String region) {
        String uf = region.toUpperCase();
        if (!PlaybackEventSimulator.REGIONS.contains(uf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF invalida: " + region);
        }
        return uf;
    }
}