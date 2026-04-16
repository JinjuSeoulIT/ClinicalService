package com.example.hospitalClinical.config;

import com.example.hospitalClinical.encounter.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(
        prefix = "app.stale-visit-auto-close",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class StaleVisitAutoCloseScheduler {

    private final EncounterService encounterService;

    @Value("${app.stale-visit-auto-close.zone:Asia/Seoul}")
    private String zoneId;

    @Scheduled(
            cron = "${app.stale-visit-auto-close.cron:0 5 0 * * *}",
            zone = "${app.stale-visit-auto-close.zone:Asia/Seoul}"
    )
    public void run() {
        ZoneId z = ZoneId.of(zoneId);
        LocalDateTime cutoff = LocalDate.now(z).atStartOfDay();
        int n = encounterService.autoCloseStaleVisits(cutoff);
        if (n > 0) {
            log.info("stale IN_PROGRESS visits auto-closed count={}", n);
        }
    }
}
