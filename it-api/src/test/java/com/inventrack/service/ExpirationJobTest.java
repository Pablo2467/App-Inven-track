package com.inventrack.service;

import com.inventrack.repository.LotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirationJobTest {
  @Mock LotRepository lots;

  @Test
  void markExpiredLots_usesTodaysDate_whenThereAreExpiredLots() {
    when(lots.markExpired(any(LocalDate.class), any(LocalDateTime.class))).thenReturn(2);
    new ExpirationJob(lots, TestData.CLOCK).markExpiredLots();
    verify(lots).markExpired(eq(TestData.TODAY), any(LocalDateTime.class));
  }

  @Test
  void markExpiredLots_doesNothingSpecial_whenNoneExpired() {
    when(lots.markExpired(any(LocalDate.class), any(LocalDateTime.class))).thenReturn(0);
    new ExpirationJob(lots, TestData.CLOCK).markExpiredLots();
    verify(lots).markExpired(eq(TestData.TODAY), any(LocalDateTime.class));
  }
}