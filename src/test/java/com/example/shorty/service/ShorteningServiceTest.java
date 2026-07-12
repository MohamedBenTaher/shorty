package com.example.shorty.service;

import com.example.shorty.model.UrlEntity;
import com.example.shorty.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShorteningServiceTest {

    @Mock
    private UrlRepository urlRepository;

    private ShorteningService service;

    @BeforeEach
    void setUp() {
        service = new ShorteningService(urlRepository);
    }

    @Test
    void shortenShouldPrependHttpsWhenMissing() {
        when(urlRepository.findByLongUrl("https://example.com")).thenReturn(Optional.empty());
        when(urlRepository.nextId()).thenReturn(1L);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String code = service.shorten("example.com");

        ArgumentCaptor<UrlEntity> captor = ArgumentCaptor.forClass(UrlEntity.class);
        verify(urlRepository).save(captor.capture());
        assertThat(captor.getValue().getLongUrl()).isEqualTo("https://example.com");
        assertThat(code).isNotBlank();
    }

    @Test
    void shortenShouldNotAlterUrlWithHttpPrefix() {
        when(urlRepository.findByLongUrl("http://example.com")).thenReturn(Optional.empty());
        when(urlRepository.nextId()).thenReturn(1L);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.shorten("http://example.com");

        ArgumentCaptor<UrlEntity> captor = ArgumentCaptor.forClass(UrlEntity.class);
        verify(urlRepository).save(captor.capture());
        assertThat(captor.getValue().getLongUrl()).isEqualTo("http://example.com");
    }

    @Test
    void shortenShouldReturnExistingShortCodeWhenUrlAlreadyPresent() {
        UrlEntity existing = new UrlEntity();
        existing.setShortCode("abc123");
        existing.setLongUrl("https://github.com");

        when(urlRepository.findByLongUrl("https://github.com")).thenReturn(Optional.of(existing));

        String code = service.shorten("https://github.com");

        assertThat(code).isEqualTo("abc123");
        verify(urlRepository, never()).nextId();
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shortenShouldSaveNewEntityWhenUrlNotPresent() {
        when(urlRepository.findByLongUrl("https://new.com")).thenReturn(Optional.empty());
        when(urlRepository.nextId()).thenReturn(5L);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String code = service.shorten("https://new.com");

        assertThat(code).isNotBlank();
        verify(urlRepository).save(any(UrlEntity.class));
    }

    @Test
    void shortenShouldEncodeSequenceIdToShortCode() {
        when(urlRepository.findByLongUrl("https://one.com")).thenReturn(Optional.empty());
        when(urlRepository.nextId()).thenReturn(10L);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String code = service.shorten("https://one.com");

        assertThat(code).isEqualTo("A");
    }

    @Test
    void shortenShouldGenerateDifferentCodesForDifferentUrls() {
        when(urlRepository.findByLongUrl(anyString())).thenReturn(Optional.empty());
        when(urlRepository.nextId()).thenReturn(1L, 2L);
        when(urlRepository.save(any(UrlEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String code1 = service.shorten("https://one.com");
        String code2 = service.shorten("https://two.com");

        assertThat(code1).isNotBlank();
        assertThat(code2).isNotBlank();
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void resolveShouldReturnLongUrlWhenShortCodeExists() {
        UrlEntity entity = new UrlEntity();
        entity.setShortCode("xyz789");
        entity.setLongUrl("https://spring.io");

        when(urlRepository.findByShortCode("xyz789")).thenReturn(Optional.of(entity));

        Optional<String> result = service.resolve("xyz789");

        assertThat(result).hasValue("https://spring.io");
    }

    @Test
    void resolveShouldReturnEmptyWhenShortCodeNotFound() {
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        Optional<String> result = service.resolve("missing");

        assertThat(result).isEmpty();
    }
}
