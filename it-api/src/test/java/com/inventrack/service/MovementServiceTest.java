package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.MovementDto;
import com.inventrack.entity.*;
import com.inventrack.repository.InventoryMovementRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MovementServiceTest {
  @Mock InventoryMovementRepository movements;
  @Captor ArgumentCaptor<Specification<InventoryMovement>> specCaptor;
  @InjectMocks MovementService service;

  private InventoryMovement movement() {
    Product p = TestData.product(1L, "JAM-001");
    InventoryMovement m = new InventoryMovement();
    m.setId(9L); m.setType(MovementType.ENTRY); m.setProduct(p);
    m.setLot(TestData.lot(4L, p, TestData.supplier(2L), "L-1", "10", TestData.TODAY, TestData.TODAY.plusDays(30)));
    m.setQuantity(new BigDecimal("10")); m.setUser(TestData.user(3L, "dueno", Role.DUENO));
    return m;
  }

  @SuppressWarnings("unchecked")
  private void runLastSpecification() {
    verify(movements, atLeastOnce()).findAll(specCaptor.capture(), any(Pageable.class));
    Root<InventoryMovement> root = mock(Root.class, RETURNS_DEEP_STUBS);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
    specCaptor.getValue().toPredicate(root, query, cb);
  }

  @Test
  void list_withAllFilters_mapsResultsAndBuildsSpecification() {
    when(movements.findAll(ArgumentMatchers.<Specification<InventoryMovement>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(movement()), PageRequest.of(0, 20), 1));

    PageResponse<MovementDto.Response> page = service.list(MovementType.ENTRY, 1L, 3L,
        TestData.TODAY.minusDays(7), TestData.TODAY, PageRequest.of(0, 20));

    assertEquals(1, page.totalElements());
    assertEquals("Entrada de proveedor", page.content().get(0).detail());
    runLastSpecification();
  }

  @Test
  void list_withoutFilters_buildsSpecification() {
    when(movements.findAll(ArgumentMatchers.<Specification<InventoryMovement>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

    PageResponse<MovementDto.Response> page = service.list(null, null, null, null, null, PageRequest.of(0, 20));

    assertEquals(0, page.totalElements());
    runLastSpecification();
  }
}