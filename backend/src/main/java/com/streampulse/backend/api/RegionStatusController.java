package com.streampulse.backend.api;

import com.streampulse.backend.detection.RegionMetrics;
import com.streampulse.backend.detection.RegionStatusStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionStatusController {

    private final RegionStatusStore store;

    public RegionStatusController(RegionStatusStore store) {
        this.store = store;
    }

    @GetMapping("/status")
    public List<RegionMetrics> status() {
        return store.findAll();
    }
}