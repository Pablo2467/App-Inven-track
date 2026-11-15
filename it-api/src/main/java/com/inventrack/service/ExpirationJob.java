package com.inventrack.service;

import com.inventrack.repository.LotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Marca como EXPIRED los lotes vencidos con existencias (al arrancar y todos los días a las 00:05). */
@Component
public class ExpirationJob {
  private static final Logger log = LoggerFactory.getLogger(ExpirationJob.class);
  private final LotRepository lots;
  private final Clock clock;

  public ExpirationJob(LotRepository lots, Clock clock) { this.lots = lots; this.clock = clock; }

  @EventListener(ApplicationReadyEvent.class)
  @Scheduled(cron = "0 5 0 * * *", zone = "America/Bogota")
  @Transactional
  public void markExpiredLots() {
    int n = lots.markExpired(LocalDate.now(clock), LocalDateTime.now(clock));
    if (n > 0) log.info("{} lote(s) marcados como EXPIRED", n);
  }
}
