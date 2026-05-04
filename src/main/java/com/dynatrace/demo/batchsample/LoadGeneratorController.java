package com.dynatrace.demo.batchsample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/load")
public class LoadGeneratorController {

    private final LoadGeneratorService loadGeneratorService;

    public LoadGeneratorController(LoadGeneratorService loadGeneratorService) {
        this.loadGeneratorService = loadGeneratorService;
    }

    @PostMapping("/start")
    public ResponseEntity<LoadGeneratorService.Status> start(
            @RequestParam(defaultValue = "100") long intervalMs) {
        loadGeneratorService.start(intervalMs);
        return ResponseEntity.ok(loadGeneratorService.getStatus());
    }

    @PostMapping("/stop")
    public ResponseEntity<LoadGeneratorService.Status> stop() {
        loadGeneratorService.stop();
        return ResponseEntity.ok(loadGeneratorService.getStatus());
    }

    @GetMapping("/status")
    public ResponseEntity<LoadGeneratorService.Status> status() {
        return ResponseEntity.ok(loadGeneratorService.getStatus());
    }
}
