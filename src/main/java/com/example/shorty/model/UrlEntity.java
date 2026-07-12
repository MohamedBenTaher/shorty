package com.example.shorty.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("urls")
public class UrlEntity implements Persistable<Long> {
    @Id Long id;
    String shortCode;
    String longUrl;
    LocalDateTime createdAt;

    @Transient
    private boolean isNew = false;

    public static UrlEntity newEntity(Long id, String shortCode, String longUrl) {
        UrlEntity entity = new UrlEntity();
        entity.id = id;
        entity.shortCode = shortCode;
        entity.longUrl = longUrl;
        entity.createdAt = LocalDateTime.now();
        entity.isNew = true;
        return entity;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
