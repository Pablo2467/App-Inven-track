package com.inventrack.service;

import com.inventrack.dto.AlertDto;
import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Lot;
import com.inventrack.entity.Product;
import com.inventrack.entity.Supplier;
import com.inventrack.repository.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertServiceTest {
  LotRepository lots = mock(LotRepository.class);
  InventoryService inventory = mock(InventoryService.class);
  AlertService service;
  Product product = TestData.product(1L, "QUE-001");
  Supplier supplier = TestData.supplier(1L);

  @BeforeEach
  void setUp() {
    service = new AlertService(lots, inventory, TestData.CLOCK);
    when(inventory.defaultDays()).thenReturn(7);
    when(lots.findDepleted(any())).thenReturn(List.of());
    when(lots.countByAvailableQuantityLessThanEqual(any())).thenReturn(0L);
    when(lots.findExpired(TestData.TODAY)).thenReturn(List.of());
    when(lots.findExpiringBetween(any(), any())).thenReturn(List.of());
    when(inventory.list(any(), any())).thenReturn(List.of());
  }

  @Test
  void expiredLot_isRed_withNegativeDays() {
    Lot expired = TestData.lot(1L, product, supplier, "V-1", "4", TestData.TODAY.minusDays(20), TestData.TODAY.minusDays(3));
    when(lots.findExpired(TestData.TODAY)).thenReturn(List.of(expired));

    AlertDto.Response r = service.get(null, null);

    assertEquals(1, r.expiredCount());
    AlertDto.Item item = r.items().get(0);
    assertEquals("EXPIRED", item.type());
    assertEquals("RED", item.severity());
    assertEquals(-3L, item.daysLeft());
  }

  @Test
  void expiringLot_isYellow_andUsesRealDates() {
    Lot soon = TestData.lot(2L, product, supplier, "P-1", "6", TestData.TODAY.minusDays(5), TestData.TODAY.plusDays(4));
    when(lots.findExpiringBetween(TestData.TODAY, TestData.TODAY.plusDays(7))).thenReturn(List.of(soon));

    AlertDto.Response r = service.get(null, null);

    assertEquals(1, r.expiringCount());
    assertEquals(7, r.thresholdDays());
    AlertDto.Item item = r.items().get(0);
    assertEquals("YELLOW", item.severity());
    assertEquals(4L, item.daysLeft());
    assertTrue(item.message().contains("4"));
  }

  @Test
  void customThreshold_isForwardedToQuery() {
    service.get(15, null);
    verify(lots).findExpiringBetween(TestData.TODAY, TestData.TODAY.plusDays(15));
  }

  @Test
  void lowStockProduct_generatesAlert_redWhenZero() {
    InventoryDto.Item zero = new InventoryDto.Item(1L, "QUE-001", "Queso", "Lácteos", "kg", new BigDecimal("4"), BigDecimal.ZERO,
        BigDecimal.ZERO, BigDecimal.ZERO, null, true, "RED");
    InventoryDto.Item ok = new InventoryDto.Item(2L, "JAM-001", "Jamón", "Embutidos", "kg", new BigDecimal("4"), new BigDecimal("30"),
        BigDecimal.ZERO, BigDecimal.ZERO, TestData.TODAY.plusDays(20), false, "GREEN");
    when(inventory.list(any(), any())).thenReturn(List.of(zero, ok));

    AlertDto.Response r = service.get(null, null);

    assertEquals(1, r.lowStockCount());
    assertEquals("LOW_STOCK", r.items().get(0).type());
    assertEquals("RED", r.items().get(0).severity());
  }

  @Test
  void depletedLots_areReportedAndCounted() {
    Lot depleted = TestData.lot(3L, product, supplier, "D-1", "1", TestData.TODAY.minusDays(9), TestData.TODAY.plusDays(9));
    depleted.setAvailableQuantity(BigDecimal.ZERO);
    when(lots.findDepleted(any())).thenReturn(List.of(depleted));
    when(lots.countByAvailableQuantityLessThanEqual(any())).thenReturn(12L);

    AlertDto.Response r = service.get(null, null);

    assertEquals(12, r.depletedCount());
    assertEquals("DEPLETED", r.items().get(0).type());
  }

  @Test
  void typeFilter_keepsOnlyRequestedType_butCountsStayComplete() {
    Lot expired = TestData.lot(1L, product, supplier, "V-1", "4", TestData.TODAY.minusDays(20), TestData.TODAY.minusDays(3));
    Lot soon = TestData.lot(2L, product, supplier, "P-1", "6", TestData.TODAY.minusDays(5), TestData.TODAY.plusDays(4));
    when(lots.findExpired(TestData.TODAY)).thenReturn(List.of(expired));
    when(lots.findExpiringBetween(any(), any())).thenReturn(List.of(soon));

    AlertDto.Response r = service.get(null, "expired");

    assertEquals(1, r.items().size());
    assertEquals("EXPIRED", r.items().get(0).type());
    assertEquals(1, r.expiringCount());
  }
}
