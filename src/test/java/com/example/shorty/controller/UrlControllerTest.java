package com.example.shorty.controller;

import com.example.shorty.dto.ShortenRequest;
import com.example.shorty.service.ShorteningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShorteningService shorteningService;

    @Test
    void shortenShouldReturnShortUrl() throws Exception {
        when(shorteningService.shorten("https://github.com")).thenReturn("aBcDeF");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"https://github.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("aBcDeF"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost/s/aBcDeF"));
    }

    @Test
    void shortenShouldReturn400WhenLongUrlIsBlank() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shortenShouldReturn400WhenLongUrlIsMissing() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirectShouldReturn302WhenShortCodeExists() throws Exception {
        when(shorteningService.resolve("aBcDeF")).thenReturn(Optional.of("https://github.com"));

        mockMvc.perform(get("/s/aBcDeF"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com"));
    }

    @Test
    void redirectShouldReturn404WhenShortCodeNotFound() throws Exception {
        when(shorteningService.resolve("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/s/unknown"))
                .andExpect(status().isNotFound());
    }
}
