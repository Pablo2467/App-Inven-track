package com.inventrack.service;

import com.inventrack.dto.ExitDto;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.InsufficientStockException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockExitServiceTest {
  StockExitRepository exits = mock(StockExitRepository.class);
  LotRepository lots = mock(LotRepository.class);
  ProductRepository products = mock(ProductRepository.class);
  UserRepository users = mock(UserRepository.class);
  InventoryMovementRepository movements = mock(InventoryMovementRepository.class);
  StockExitService service;
  Product product = TestData.product(1L, "JAM-001");
  Supplier supplier = TestData.supplier(2L);
  Lot oldest, newest;

  @BeforeEach
  void setUp() {
    service = new StockExitService(exits, lots, products, users, movements, TestData.CLOCK);
    when(users.findById(1L)).thenReturn(Optional.of(TestData.user(1L, "dueno", Role.DUENO)));
    when(products.findById(1L)).thenReturn(Optional.of(product));
    when(exits.save(any(StockExit.class))).thenAnswer(i -> i.getArgument(0));
    when(lots.save(any(Lot.class))).thenAnswer(i -> i.getArgument(0));
    oldest = TestData.lot(10L, product, supplier, "VIEJO", "5", TestData.TODAY.minusDays(10), TestData.TODAY.plusDays(20));
    newest = TestData.lot(11L, product, supplier, "NUEVO", "10", TestData.TODAY.minusDays(3), TestData.TODAY.plusDays(27));
    // El repositorio ya devuelve los lotes en orden FIFO (ORDER BY entry_date, id) y bloqueados
    when(lots.lockUsableLots(1L, TestData.TODAY)).thenReturn(List.of(oldest, newest));
  }

  private ExitDto.Request request(String qty, ExitReason reason) {
    return new ExitDto.Request(1L, new BigDecimal(qty), reason, null, "prueba");
  }

  @Test
  void fifo_consumesOldestLotFirst_thenNextOne() {
    ExitDto.Response r = service.register(request("8", ExitReason.SALE), TestData.ACTOR);

    assertEquals(0, oldest.getAvailableQuantity().compareTo(BigDecimal.ZERO), "El lote más antiguo debe quedar en 0");
    assertEquals(LotStatus.DEPLETED, oldest.getStatus());
    assertEquals(0, newest.getAvailableQuantity().compareTo(new BigDecimal("7")), "El lote nuevo cede solo 3 unidades");
    assertEquals(LotStatus.AVAILABLE, newest.getStatus());

    assertEquals(2, r.allocations().size());
    assertEquals("VIEJO", r.allocations().get(0).lotNumber());
    assertEquals(0, r.allocations().get(0).quantity().compareTo(new BigDecimal("5")));
    assertEquals("NUEVO", r.allocations().get(1).lotNumber());
    assertEquals(0, r.allocations().get(1).quantity().compareTo(new BigDecimal("3")));
  }

  @Test
  void fifo_smallExit_onlyTouchesOldestLot() {
    service.register(request("2", ExitReason.SALE), TestData.ACTOR);
    assertEquals(0, oldest.getAvailableQuantity().compareTo(new BigDecimal("3")));
    assertEquals(0, newest.getAvailableQuantity().compareTo(new BigDecimal("10")));
    verify(movements, times(1)).save(any(InventoryMovement.class));
  }

  @Test
  void exit_createsOneExitMovementPerLot_withResponsibleUser() {
    service.register(request("8", ExitReason.SALE), TestData.ACTOR);
    ArgumentCaptor<InventoryMovement> m = ArgumentCaptor.forClass(InventoryMovement.class);
    verify(movements, times(2)).save(m.capture());
    assertTrue(m.getAllValues().stream().allMatch(x -> x.getType() == MovementType.EXIT && x.getUser().getId() == 1L));
    BigDecimal total = m.getAllValues().stream().map(InventoryMovement::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, total.compareTo(new BigDecimal("8")));
  }

  @Test
  void exit_exactlyAllStock_depletesEveryLot() {
    service.register(request("15", ExitReason.SALE), TestData.ACTOR);
    assertEquals(LotStatus.DEPLETED, oldest.getStatus());
    assertEquals(LotStatus.DEPLETED, newest.getStatus());
  }

  @Test
  void exit_moreThanAvailable_isRejected_andNothingIsModified() {
    InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> service.register(request("16", ExitReason.SALE), TestData.ACTOR));
    assertTrue(ex.getMessage().contains("15"), "El mensaje debe informar lo disponible");
    assertEquals(0, oldest.getAvailableQuantity().compareTo(new BigDecimal("5")));
    assertEquals(0, newest.getAvailableQuantity().compareTo(new BigDecimal("10")));
    verify(exits, never()).save(any());
    verify(movements, never()).save(any());
  }

  @Test
  void exit_withNoLotsAtAll_isRejected() {
    when(lots.lockUsableLots(1L, TestData.TODAY)).thenReturn(List.of());
    assertThrows(InsufficientStockException.class, () -> service.register(request("1", ExitReason.SALE), TestData.ACTOR));
  }

  @Test
  void expiredDisposal_usesOnlyExpiredLots() {
    Lot expired = TestData.lot(12L, product, supplier, "VENCIDO", "4", TestData.TODAY.minusDays(40), TestData.TODAY.minusDays(5));
    when(lots.lockExpiredLots(1L, TestData.TODAY)).thenReturn(List.of(expired));
    ExitDto.Response r = service.register(request("4", ExitReason.EXPIRED_DISPOSAL), TestData.ACTOR);
    assertEquals(LotStatus.DEPLETED, expired.getStatus());
    assertEquals("VENCIDO", r.allocations().get(0).lotNumber());
    verify(lots, never()).lockUsableLots(any(), any());
  }

  @Test
  void exit_futureDate_isRejected() {
    ExitDto.Request r = new ExitDto.Request(1L, BigDecimal.ONE, ExitReason.SALE, TestData.TODAY.plusDays(1), null);
    assertThrows(BadRequestException.class, () -> service.register(r, TestData.ACTOR));
  }

  @Test
  void exit_unknownProduct_isNotFound() {
    when(products.findById(99L)).thenReturn(Optional.empty());
    ExitDto.Request r = new ExitDto.Request(99L, BigDecimal.ONE, ExitReason.SALE, null, null);
    assertThrows(NotFoundException.class, () -> service.register(r, TestData.ACTOR));
  }
}
