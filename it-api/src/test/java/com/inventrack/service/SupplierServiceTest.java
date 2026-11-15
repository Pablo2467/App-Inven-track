package com.inventrack.service;

import com.inventrack.dto.SupplierDto;
import com.inventrack.entity.Status;
import com.inventrack.entity.Supplier;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.StockEntryRepository;
import com.inventrack.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupplierServiceTest {
  @Mock SupplierRepository suppliers;
  @Mock LotRepository lots;
  @Mock StockEntryRepository entries;
  @InjectMocks SupplierService service;

  private SupplierDto.Request request(String taxId) {
    return new SupplierDto.Request("Distribuciones del Huila", taxId, "3001234567", "ventas@test.com", "Neiva", null);
  }

  @Test
  void create_savesActiveSupplier() {
    when(suppliers.existsByTaxIdIgnoreCase("900123456-1")).thenReturn(false);
    when(suppliers.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));
    SupplierDto.Response r = service.create(request("900123456-1"));
    assertEquals("Distribuciones del Huila", r.name());
    assertEquals(Status.ACTIVE, r.status());
  }

  @Test
  void create_duplicateTaxId_throwsConflict() {
    when(suppliers.existsByTaxIdIgnoreCase("900123456-1")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(request("900123456-1")));
  }

  @Test
  void get_notFound_throws() {
    when(suppliers.findById(5L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(5L));
  }

  @Test
  void update_duplicateTaxId_throwsConflict() {
    when(suppliers.findById(1L)).thenReturn(Optional.of(TestData.supplier(1L)));
    when(suppliers.existsByTaxIdIgnoreCaseAndIdNot("900999", 1L)).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.update(1L, request("900999")));
  }

  @Test
  void update_changesData() {
    when(suppliers.findById(1L)).thenReturn(Optional.of(TestData.supplier(1L)));
    when(suppliers.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));
    assertEquals("900555", service.update(1L, request("900555")).taxId());
  }

  @Test
  void delete_withEntries_deactivates() {
    Supplier s = TestData.supplier(1L);
    when(suppliers.findById(1L)).thenReturn(Optional.of(s));
    when(entries.existsBySupplierId(1L)).thenReturn(true);
    assertEquals("DEACTIVATED", service.delete(1L).result());
    assertEquals(Status.INACTIVE, s.getStatus());
  }

  @Test
  void delete_withoutHistory_deletes() {
    Supplier s = TestData.supplier(1L);
    when(suppliers.findById(1L)).thenReturn(Optional.of(s));
    assertEquals("DELETED", service.delete(1L).result());
    verify(suppliers).delete(s);
  }
}
