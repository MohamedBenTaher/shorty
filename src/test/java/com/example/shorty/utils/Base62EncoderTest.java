package com.example.shorty.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base64EncoderTest {

    @Test
    void encodeZeroReturnsZero() {
        assertThat(Base64Encoder.encode(0L)).isEqualTo("0");
    }

    @Test
    void encodeSingleDigit() {
        assertThat(Base64Encoder.encode(5L)).isEqualTo("5");
    }

    @Test
    void encodeToA() {
        assertThat(Base64Encoder.encode(10L)).isEqualTo("A");
    }

    @Test
    void encodeToZ() {
        assertThat(Base64Encoder.encode(35L)).isEqualTo("Z");
    }

    @Test
    void encodeToa() {
        assertThat(Base64Encoder.encode(36L)).isEqualTo("a");
    }

    @Test
    void encodeFirstRollover() {
        assertThat(Base64Encoder.encode(62L)).isEqualTo("10");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "5, 5",
            "A, 10",
            "Z, 35",
            "a, 36",
            "z, 61",
            "10, 62"
    })
    void decodeReturnsExpectedValue(String input, long expected) {
        assertThat(Base64Encoder.decode(input)).isEqualTo(expected);
    }

    @Test
    void decodeEmptyStringThrows() {
        assertThatThrownBy(() -> Base64Encoder.decode(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decodeInvalidCharThrows() {
        assertThatThrownBy(() -> Base64Encoder.decode("abc$123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 62L, 1000L, 999999L, Long.MAX_VALUE})
    void encodeDecodeRoundTrip(long original) {
        String encoded = Base64Encoder.encode(original);
        long decoded = Base64Encoder.decode(encoded);
        assertThat(decoded).isEqualTo(original);
    }
}