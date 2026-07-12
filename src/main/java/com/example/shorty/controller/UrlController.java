package com.example.shorty.controller;

import com.example.shorty.dto.ShortenRequest;
import com.example.shorty.dto.ShortenResponse;
import com.example.shorty.service.ShorteningService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class UrlController {

    private final ShorteningService shorteningService;

    public UrlController(ShorteningService shorteningService) {
        this.shorteningService = shorteningService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        String shortCode = shorteningService.shorten(request.longUrl());
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/s/{shortCode}")
            .build(shortCode)
            .toString();
        return ResponseEntity.ok(new ShortenResponse(shortUrl, shortCode));
    }

    @GetMapping("/s/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String longUrl = shorteningService.resolve(shortCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, longUrl)
            .build();
    }
}
