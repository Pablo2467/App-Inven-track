package com.inventrack.service;

import com.inventrack.dto.AlertDto;
import com.inventrack.dto.DashboardDto;
import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Status;
import com.inventrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {
  @Mock ProductRepository products;
  @Mock SupplierRepository suppliers;
  @Mock UserRepository users;
  @Mock LotRepository lots;
  @Mock StockEntryRepository entries;
  @Mock StockExitRepository exits;
  @Mock InventoryMovementRepository movements;
  @Mock InventoryService inventory;
  @Mock AlertService alerts;
  DashboardService service;

  private static List<Object[]> rows(Object[]... rows) { return Arrays.asList(rows); }

  private static InventoryDto.Item item(Long id, String name, String stock, String status) {
    return new InventoryDto.Item(id, "C" + id, name, "Cat", "kg", new BigDecimal("5"), new BigDecimal(stock),
        BigDecimal.ZERO, BigDecimal.ZERO, null, false, status);
  }

  private static AlertDto.Item alert(String type, String severity) {
    return new AlertDto.Item(type, severity, 1L, "Jamón", 1L, "L-1", BigDecimal.ONE, TestData.TODAY, 3L, "mensaje");
  }

  @BeforeEach
  void setUp() {
    service = new DashboardService(products, suppliers, users, lots, entries, exits, movements, inventory, alerts, TestData.CLOCK);

    when(products.countByStatus(Status.ACTIVE)).thenReturn(4L);
    when(suppliers.countByStatus(Status.ACTIVE)).thenReturn(2L);
    when(users.countByStatus(Status.ACTIVE)).thenReturn(3L);
    when(lots.sumUsableStock(TestData.TODAY)).thenReturn(new BigDecimal("75"));
    when(entries.count()).thenReturn(10L);
    when(exits.count()).thenReturn(6L);
    when(entries.dailyTotals(any(LocalDate.class))).thenReturn(rows(new Object[]{TestData.TODAY, new BigDecimal("12")}));
    when(exits.dailyTotals(any(LocalDate.class))).thenReturn(rows(new Object[]{TestData.TODAY.minusDays(1), new BigDecimal("3")}));
    when(movements.findTop8ByOrderByIdDesc()).thenReturn(List.of());
    when(inventory.list(null, null)).thenReturn(List.of(
        item(1L, "Jamón", "20", "GREEN"), item(2L, "Queso", "2", "YELLOW"), item(3L, "Chorizo", "0", "RED")));
    when(alerts.get(null, null)).thenReturn(new AlertDto.Response(1, 1, 1, 1, 7,
        List.of(alert("EXPIRING", "YELLOW"), alert("DEPLETED", "GREEN"), alert("EXPIRED", "RED"))));
  }

  @Test
  void get_aggregatesCountersAndTrafficLights() {
    DashboardDto.Response r = service.get();

    assertEquals(4, r.totalProducts());
    assertEquals(2, r.totalSuppliers());
    assertEquals(3, r.activeUsers());
    assertEquals(new BigDecimal("75"), r.availableUnits());
    assertEquals(10, r.totalEntries());
    assertEquals(6, r.totalExits());
    assertEquals(1, r.expiringLots());
    assertEquals(1, r.expiredLots());
    assertEquals(1, r.depletedLots());
    assertEquals(1, r.greenProducts());
    assertEquals(1, r.yellowProducts());
    assertEquals(1, r.redProducts());
    assertTrue(r.recentMovements().isEmpty());
  }

  @Test
  void get_buildsFourteenDayChart_withZerosForEmptyDays() {
    DashboardDto.Response r = service.get();

    assertEquals(14, r.chart().size());
    DashboardDto.DailyPoint last = r.chart().get(13);
    assertEquals(TestData.TODAY, last.date());
    assertEquals(new BigDecimal("12"), last.entries());
    assertEquals(BigDecimal.ZERO, last.exits());
    assertEquals(new BigDecimal("3"), r.chart().get(12).exits());
    assertEquals(BigDecimal.ZERO, r.chart().get(0).entries());
  }

  @Test
  void get_ordersAlertsBySeverity_andLowestStockFirst() {
    DashboardDto.Response r = service.get();

    assertEquals("RED", r.alerts().get(0).severity());
    assertEquals("YELLOW", r.alerts().get(1).severity());
    assertEquals("GREEN", r.alerts().get(2).severity());
    assertEquals("Chorizo", r.lowStockProducts().get(0).name());
  }
}