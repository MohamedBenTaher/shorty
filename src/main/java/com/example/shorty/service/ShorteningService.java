package com.example.shorty.service;

import com.example.shorty.model.UrlEntity;
import com.example.shorty.repository.UrlRepository;
import com.example.shorty.utils.Base64Encoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ShorteningService {

    private final UrlRepository urlRepository;

    public ShorteningService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String shorten(String longUrl) {
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "https://" + longUrl;
        }

        String finalLongUrl = longUrl;
        Optional<UrlEntity> existing = urlRepository.findByLongUrl(finalLongUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        long id = urlRepository.nextId();
        String shortCode = Base64Encoder.encode(id);

        UrlEntity entity = new UrlEntity();
        entity.setId(id);
        entity.setShortCode(shortCode);
        entity.setLongUrl(finalLongUrl);
        entity.setCreatedAt(LocalDateTime.now());
        urlRepository.save(entity);

        return shortCode;
    }

    public Optional<String> resolve(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
            .map(UrlEntity::getLongUrl);
    }
}
