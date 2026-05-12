package com.dynatrace.demo.batchsample2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/load")
public class LoadGeneratorController {
    private final DemoLoadGenerator generator;

    public LoadGeneratorController(DemoLoadGenerator generator) {
        this.generator = generator;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam(defaultValue = "100") long intervalMs) {
        generator.start(intervalMs);
        return generator.status();
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        generator.stop();
        return generator.status();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return generator.status();
    }
}
