package com.example.shorty.dto;

import jakarta.validation.constraints.NotBlank;

public record ShortenRequest(@NotBlank String longUrl) {
}
