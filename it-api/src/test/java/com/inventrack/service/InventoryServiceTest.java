package com.inventrack.service;

import com.inventrack.dto.InventoryDto;
import com.inventrack.entity.Lot;
import com.inventrack.entity.Product;
import com.inventrack.entity.Supplier;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.ProductRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryServiceTest {
  @Mock ProductRepository products;
  @Mock LotRepository lots;
  @Captor ArgumentCaptor<Specification<Product>> specCaptor;
  InventoryService service;

  @BeforeEach
  void setUp() {
    service = new InventoryService(products, lots, TestData.CLOCK, 7);
    Product jamon = TestData.product(1L, "JAM-001");
    Product queso = TestData.product(2L, "QUE-001");
    Product chorizo = TestData.product(3L, "CHO-001");
    Supplier sup = TestData.supplier(9L);

    when(products.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Sort.class))).thenReturn(List.of(jamon, queso, chorizo));
    when(lots.findAllWithStock()).thenReturn(List.of(
        TestData.lot(10L, jamon, sup, "J-1", "20", TestData.TODAY.minusDays(3), TestData.TODAY.plusDays(20)),
        TestData.lot(11L, chorizo, sup, "C-1", "4", TestData.TODAY.minusDays(30), TestData.TODAY.minusDays(1))));
  }

  @Test
  void defaultDays_comesFromConfiguration() {
    assertEquals(7, service.defaultDays());
  }

  @Test
  void list_calculatesStockAndTrafficLightPerProduct() {
    List<InventoryDto.Item> items = service.list(null, null);

    assertEquals(3, items.size());
    assertEquals("GREEN", items.get(0).status());   // jamón: 20 vigentes, nada por vencer
    assertEquals("RED", items.get(1).status());     // queso: sin lotes
    assertEquals("RED", items.get(2).status());     // chorizo: lote vencido
    assertEquals(0, items.get(2).expiredQuantity().compareTo(new java.math.BigDecimal("4")));
  }

  @Test
  void list_withWiderWindow_marksExpiringProductsYellow() {
    List<InventoryDto.Item> items = service.list(null, 30);
    assertEquals("YELLOW", items.get(0).status());
    assertEquals(TestData.TODAY.plusDays(20), items.get(0).nearestExpiration());
  }

  @Test
  @SuppressWarnings("unchecked")
  void list_buildsSearchSpecification() {
    service.list(" jam ", null);
    service.list(null, null);
    verify(products, atLeastOnce()).findAll(specCaptor.capture(), any(Sort.class));

    for (Specification<Product> spec : specCaptor.getAllValues()) {
      Root<Product> root = mock(Root.class, RETURNS_DEEP_STUBS);
      CriteriaQuery<?> query = mock(CriteriaQuery.class);
      CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
      spec.toPredicate(root, query, cb);
    }
  }
}