package com.inventrack.service;

import com.inventrack.dto.*;
import com.inventrack.entity.*;
import com.inventrack.mapper.Mappers;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappersTest {
  private final Product jamon = TestData.product(1L, "JAM-001");
  private final Supplier sup = TestData.supplier(2L);
  private final User owner = TestData.user(3L, "dueno", Role.DUENO);
  private final Lot lot = TestData.lot(4L, jamon, sup, "L-1", "10", TestData.TODAY.minusDays(2), TestData.TODAY.plusDays(5));

  @Test
  void product_copiesFields() {
    ProductDto.Response r = Mappers.product(jamon);
    assertEquals("JAM-001", r.code());
    assertEquals(Status.ACTIVE, r.status());
    assertEquals(new BigDecimal("5"), r.minStock());
  }

  @Test
  void supplier_copiesFields() {
    SupplierDto.Response r = Mappers.supplier(sup);
    assertEquals(2L, r.id());
    assertEquals("Proveedor 2", r.name());
    assertEquals("9002", r.taxId());
  }

  @Test
  void user_copiesFields_withoutPassword() {
    UserDto.Response r = Mappers.user(owner);
    assertEquals("dueno", r.username());
    assertEquals(Role.DUENO, r.role());
    assertEquals("Usuario dueno", r.fullName());
  }

  @Test
  void lot_availableDepletedAndExpired() {
    LotDto.Response available = Mappers.lot(lot, TestData.TODAY);
    assertEquals(LotStatus.AVAILABLE, available.status());
    assertEquals(5L, available.daysToExpire());
    assertEquals("JAM-001", available.productCode());

    lot.setAvailableQuantity(BigDecimal.ZERO);
    assertEquals(LotStatus.DEPLETED, Mappers.lot(lot, TestData.TODAY).status());

    lot.setAvailableQuantity(BigDecimal.ONE);
    lot.setExpirationDate(TestData.TODAY.minusDays(1));
    LotDto.Response expired = Mappers.lot(lot, TestData.TODAY);
    assertEquals(LotStatus.EXPIRED, expired.status());
    assertEquals(-1L, expired.daysToExpire());
  }

  private StockEntry entry() {
    StockEntry e = new StockEntry();
    e.setId(7L); e.setProduct(jamon); e.setSupplier(sup); e.setLot(lot);
    e.setQuantity(new BigDecimal("10")); e.setEntryDate(TestData.TODAY); e.setUser(owner); e.setNotes("nota");
    return e;
  }

  private StockExit exit() {
    StockExit x = new StockExit();
    x.setId(8L); x.setProduct(jamon); x.setQuantity(new BigDecimal("3")); x.setReason(ExitReason.SALE);
    x.setExitDate(TestData.TODAY); x.setUser(owner);
    return x;
  }

  private InventoryMovement movement(MovementType type) {
    InventoryMovement m = new InventoryMovement();
    m.setId(9L); m.setType(type); m.setProduct(jamon); m.setLot(lot); m.setQuantity(new BigDecimal("3")); m.setUser(owner);
    return m;
  }

  @Test
  void entry_copiesFields() {
    EntryDto.Response r = Mappers.entry(entry());
    assertEquals(7L, r.id());
    assertEquals("L-1", r.lotNumber());
    assertEquals(lot.getExpirationDate(), r.expirationDate());
    assertEquals("Usuario dueno", r.userName());
  }

  @Test
  void exit_copiesFields_andAllocations() {
    ExitDto.Response r = Mappers.exit(exit(), List.of());
    assertEquals(8L, r.id());
    assertEquals(ExitReason.SALE, r.reason());
    assertTrue(r.allocations().isEmpty());
  }

  @Test
  void movement_entry_hasSupplierDetail() {
    InventoryMovement m = movement(MovementType.ENTRY);
    m.setEntry(entry());
    MovementDto.Response r = Mappers.movement(m);
    assertEquals("Entrada de proveedor", r.detail());
    assertEquals(7L, r.entryId());
    assertNull(r.outputId());
  }

  @Test
  void movement_exit_hasReasonDetail() {
    InventoryMovement m = movement(MovementType.EXIT);
    m.setOutput(exit());
    MovementDto.Response r = Mappers.movement(m);
    assertEquals("Salida: SALE", r.detail());
    assertEquals(8L, r.outputId());
    assertNull(r.entryId());
  }
}
