package com.logitrack.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FixedClockTestConfig {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    public static final Instant INSTANTE = Instant.parse("2026-08-24T11:00:00Z");

    @Bean
    @Primary
    Clock fixedTestClock() {
        return Clock.fixed(INSTANTE, ZONA);
    }
}
