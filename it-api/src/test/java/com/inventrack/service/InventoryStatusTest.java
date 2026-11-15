package com.inventrack.service;

import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Lot;
import com.inventrack.entity.Product;
import com.inventrack.entity.Supplier;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Semáforo verde / amarillo / rojo calculado desde los lotes. */
class InventoryStatusTest {
  Product product = TestData.product(1L, "JAM-001");   // stock mínimo 5
  Supplier supplier = TestData.supplier(1L);
  LocalDate today = TestData.TODAY;
  LocalDate limit = today.plusDays(7);

  private Lot lot(String qty, int daysToExpire) {
    return TestData.lot(1L, product, supplier, "L", qty, today.minusDays(5), today.plusDays(daysToExpire));
  }

  @Test
  void healthyStock_isGreen() {
    InventoryDto.Item i = InventoryService.item(product, List.of(lot("20", 25)), today, limit);
    assertEquals("GREEN", i.status());
    assertEquals(0, i.stock().compareTo(new BigDecimal("20")));
    assertFalse(i.lowStock());
  }

  @Test
  void lotExpiringSoon_isYellow() {
    InventoryDto.Item i = InventoryService.item(product, List.of(lot("20", 3)), today, limit);
    assertEquals("YELLOW", i.status());
    assertEquals(0, i.expiringQuantity().compareTo(new BigDecimal("20")));
  }

  @Test
  void lowStock_isYellow() {
    InventoryDto.Item i = InventoryService.item(product, List.of(lot("4", 25)), today, limit);
    assertTrue(i.lowStock());
    assertEquals("YELLOW", i.status());
  }

  @Test
  void expiredUnits_areRed_andExcludedFromStock() {
    InventoryDto.Item i = InventoryService.item(product, List.of(lot("10", -2), lot("20", 25)), today, limit);
    assertEquals("RED", i.status());
    assertEquals(0, i.expiredQuantity().compareTo(new BigDecimal("10")));
    assertEquals(0, i.stock().compareTo(new BigDecimal("20")));
  }

  @Test
  void noStock_isRed() {
    assertEquals("RED", InventoryService.item(product, List.of(), today, limit).status());
  }
}
