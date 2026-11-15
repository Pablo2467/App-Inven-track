package com.inventrack.service;

import com.inventrack.dto.EntryDto;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockEntryServiceTest {
  StockEntryRepository entries = mock(StockEntryRepository.class);
  LotRepository lots = mock(LotRepository.class);
  ProductRepository products = mock(ProductRepository.class);
  SupplierRepository suppliers = mock(SupplierRepository.class);
  UserRepository users = mock(UserRepository.class);
  InventoryMovementRepository movements = mock(InventoryMovementRepository.class);
  StockEntryService service;
  Product product = TestData.product(1L, "JAM-001");
  Supplier supplier = TestData.supplier(2L);

  @BeforeEach
  void setUp() {
    service = new StockEntryService(entries, lots, products, suppliers, users, movements, TestData.CLOCK, 30);
    when(users.findById(1L)).thenReturn(Optional.of(TestData.user(1L, "dueno", Role.DUENO)));
    when(products.findById(1L)).thenReturn(Optional.of(product));
    when(suppliers.findById(2L)).thenReturn(Optional.of(supplier));
    when(lots.save(any(Lot.class))).thenAnswer(i -> i.getArgument(0));
    when(entries.save(any(StockEntry.class))).thenAnswer(i -> i.getArgument(0));
  }

  private EntryDto.Request request(String lot, String qty, LocalDate entry, LocalDate expiration) {
    return new EntryDto.Request(1L, 2L, lot, new BigDecimal(qty), entry, expiration, new BigDecimal("12000"), null);
  }

  @Test
  void register_withoutExpiration_defaultsTo30DaysAfterEntry() {
    EntryDto.Response r = service.register(request("L-1", "10", null, null), TestData.ACTOR);

    ArgumentCaptor<Lot> lot = ArgumentCaptor.forClass(Lot.class);
    verify(lots).save(lot.capture());
    assertEquals(TestData.TODAY, lot.getValue().getEntryDate());
    assertEquals(TestData.TODAY.plusDays(30), lot.getValue().getExpirationDate());
    assertEquals(lot.getValue().getInitialQuantity(), lot.getValue().getAvailableQuantity());
    assertEquals(LotStatus.AVAILABLE, lot.getValue().getStatus());
    assertEquals(TestData.TODAY.plusDays(30), r.expirationDate());
  }

  @Test
  void register_withCustomExpiration_respectsIt() {
    LocalDate custom = TestData.TODAY.plusDays(10);
    service.register(request("L-2", "5", null, custom), TestData.ACTOR);
    ArgumentCaptor<Lot> lot = ArgumentCaptor.forClass(Lot.class);
    verify(lots).save(lot.capture());
    assertEquals(custom, lot.getValue().getExpirationDate());
  }

  @Test
  void register_createsEntryAndEntryMovement_withResponsibleUser() {
    service.register(request("L-3", "7.5", null, null), TestData.ACTOR);

    ArgumentCaptor<InventoryMovement> m = ArgumentCaptor.forClass(InventoryMovement.class);
    verify(movements).save(m.capture());
    assertEquals(MovementType.ENTRY, m.getValue().getType());
    assertEquals(new BigDecimal("7.5"), m.getValue().getQuantity());
    assertEquals(1L, m.getValue().getUser().getId());
    verify(entries).save(any(StockEntry.class));
  }

  @Test
  void register_withoutLotNumber_generatesOne() {
    EntryDto.Response r = service.register(request(null, "3", null, null), TestData.ACTOR);
    assertTrue(r.lotNumber().startsWith("LOT-JAM-001-20261114-"));
  }

  @Test
  void register_expirationBeforeEntry_isRejected() {
    assertThrows(BadRequestException.class, () -> service.register(request("L-4", "1", TestData.TODAY, TestData.TODAY.minusDays(1)), TestData.ACTOR));
    verify(lots, never()).save(any());
  }

  @Test
  void register_futureEntryDate_isRejected() {
    assertThrows(BadRequestException.class, () -> service.register(request("L-5", "1", TestData.TODAY.plusDays(1), null), TestData.ACTOR));
  }

  @Test
  void register_duplicateLotNumber_isRejected() {
    when(lots.existsByProductIdAndLotNumberIgnoreCase(1L, "L-6")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.register(request("L-6", "1", null, null), TestData.ACTOR));
  }

  @Test
  void register_inactiveProduct_isRejected() {
    product.setStatus(Status.INACTIVE);
    assertThrows(BadRequestException.class, () -> service.register(request("L-7", "1", null, null), TestData.ACTOR));
  }
}
