package com.example.shorty.controller;

import com.example.shorty.model.UrlEntity;
import com.example.shorty.repository.UrlRepository;
import com.example.shorty.utils.Base64Encoder;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
public class UrlController {

    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody Map<String, String> body) {
        String longUrl = body.get("longUrl");
        if (longUrl == null || longUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "https://" + longUrl;
        }

        String finalLongUrl = longUrl;
        Optional<UrlEntity> existing = urlRepository.findByLongUrl(finalLongUrl);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("shortUrl", "http://localhost:8080/" + existing.get().getShortCode()));
        }

        SecureRandom random = new SecureRandom();
        String shortCode = Base64Encoder.encode(random.nextLong() & Long.MAX_VALUE);

        UrlEntity entity = new UrlEntity();
        entity.setShortCode(shortCode);
        entity.setLongUrl(finalLongUrl);
        entity.setCreatedAt(LocalDateTime.now());
        urlRepository.save(entity);

        return ResponseEntity.ok(Map.of("shortUrl", "http://localhost:8080/s/" + shortCode));
    }

    @GetMapping("/s/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<UrlEntity> entity = urlRepository.findByShortCode(shortCode);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(entity.get().getLongUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
