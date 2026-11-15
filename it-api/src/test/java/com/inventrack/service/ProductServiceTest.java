package com.inventrack.service;

import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.dto.ProductDto;
import com.inventrack.entity.Product;
import com.inventrack.entity.Status;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.repository.InventoryMovementRepository;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {
  @Mock ProductRepository products;
  @Mock LotRepository lots;
  @Mock InventoryMovementRepository movements;
  @InjectMocks ProductService service;

  private ProductDto.Request request(String code) {
    return new ProductDto.Request(code, "Jamón de cerdo", "Jamón", "Embutidos", "kg", new BigDecimal("5"), null);
  }

  @Test
  void create_normalizesCodeAndSaves() {
    when(products.existsByCodeIgnoreCase("JAM-001")).thenReturn(false);
    when(products.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductDto.Response r = service.create(request(" jam-001 "));

    assertEquals("JAM-001", r.code());
    assertEquals(Status.ACTIVE, r.status());
    assertEquals(new BigDecimal("5"), r.minStock());
  }

  @Test
  void create_duplicateCode_throwsConflict() {
    when(products.existsByCodeIgnoreCase("JAM-001")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(request("JAM-001")));
    verify(products, never()).save(any());
  }

  @Test
  void get_notFound_throws() {
    when(products.findById(99L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(99L));
  }

  @Test
  void get_existing_returnsProduct() {
    when(products.findById(1L)).thenReturn(Optional.of(TestData.product(1L, "JAM-001")));
    assertEquals("JAM-001", service.get(1L).code());
  }

  @Test
  void update_changesFields() {
    when(products.findById(1L)).thenReturn(Optional.of(TestData.product(1L, "JAM-001")));
    when(products.existsByCodeIgnoreCaseAndIdNot("JAM-002", 1L)).thenReturn(false);
    when(products.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductDto.Response r = service.update(1L, request("jam-002"));

    assertEquals("JAM-002", r.code());
    assertEquals("Jamón de cerdo", r.name());
  }

  @Test
  void update_codeUsedByAnotherProduct_throwsConflict() {
    when(products.findById(1L)).thenReturn(Optional.of(TestData.product(1L, "JAM-001")));
    when(products.existsByCodeIgnoreCaseAndIdNot("QUE-001", 1L)).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.update(1L, request("QUE-001")));
  }

  @Test
  void delete_withHistory_deactivatesInsteadOfDeleting() {
    Product p = TestData.product(1L, "JAM-001");
    when(products.findById(1L)).thenReturn(Optional.of(p));
    when(lots.existsByProductId(1L)).thenReturn(true);

    MessageResponse r = service.delete(1L);

    assertEquals("DEACTIVATED", r.result());
    assertEquals(Status.INACTIVE, p.getStatus());
    verify(products, never()).delete(any(Product.class));
  }

  @Test
  void delete_withoutHistory_deletes() {
    Product p = TestData.product(1L, "JAM-001");
    when(products.findById(1L)).thenReturn(Optional.of(p));
    when(lots.existsByProductId(1L)).thenReturn(false);
    when(movements.existsByProductId(1L)).thenReturn(false);

    assertEquals("DELETED", service.delete(1L).result());
    verify(products).delete(p);
  }
}
