package com.example.shorty.service;

import com.example.shorty.id.SnowflakeIdGenerator;
import com.example.shorty.model.UrlEntity;
import com.example.shorty.repository.UrlRepository;
import com.example.shorty.utils.Base64Encoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShorteningService {

    private final UrlRepository urlRepository;
    private final SnowflakeIdGenerator idGenerator;

    public ShorteningService(UrlRepository urlRepository, SnowflakeIdGenerator idGenerator) {
        this.urlRepository = urlRepository;
        this.idGenerator = idGenerator;
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

        long id = idGenerator.nextId();
        String shortCode = Base64Encoder.encode(id);

        UrlEntity entity = UrlEntity.newEntity(id, shortCode, finalLongUrl);
        urlRepository.save(entity);

        return shortCode;
    }

    public Optional<String> resolve(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
            .map(UrlEntity::getLongUrl);
    }
}
