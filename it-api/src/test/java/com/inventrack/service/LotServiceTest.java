package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.LotDto;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.repository.LotRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LotServiceTest {
  @Mock LotRepository lots;
  @Captor ArgumentCaptor<Specification<Lot>> specCaptor;
  LotService service;
  Lot lot;

  @BeforeEach
  void setUp() {
    service = new LotService(lots, TestData.CLOCK);
    lot = TestData.lot(10L, TestData.product(1L, "JAM-001"), TestData.supplier(2L), "L-1", "10",
        TestData.TODAY.minusDays(2), TestData.TODAY.plusDays(20));
    when(lots.findById(10L)).thenReturn(Optional.of(lot));
    when(lots.save(any(Lot.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void get_returnsMappedLot() {
    LotDto.Response r = service.get(10L);
    assertEquals("L-1", r.lotNumber());
    assertEquals(LotStatus.AVAILABLE, r.status());
    assertEquals(20L, r.daysToExpire());
  }

  @Test
  void get_notFound_throws() {
    when(lots.findById(99L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.get(99L));
  }

  @Test
  void update_changesNumberExpirationAndCost() {
    LotDto.Response r = service.update(10L, new LotDto.UpdateRequest(" L-2 ", TestData.TODAY.plusDays(30), new BigDecimal("1500.50")));
    assertEquals("L-2", r.lotNumber());
    assertEquals(TestData.TODAY.plusDays(30), r.expirationDate());
    assertEquals(new BigDecimal("1500.50"), r.unitCost());
  }

  @Test
  void update_duplicateNumber_throwsConflict() {
    when(lots.existsByProductIdAndLotNumberIgnoreCaseAndIdNot(1L, "L-2", 10L)).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.update(10L, new LotDto.UpdateRequest("L-2", TestData.TODAY.plusDays(30), null)));
    verify(lots, never()).save(any());
  }

  @Test
  void update_expirationBeforeEntry_throwsBadRequest() {
    assertThrows(BadRequestException.class,
        () -> service.update(10L, new LotDto.UpdateRequest("L-1", lot.getEntryDate().minusDays(1), null)));
    verify(lots, never()).save(any());
  }

  @SuppressWarnings("unchecked")
  private void runLastSpecification() {
    verify(lots, atLeastOnce()).findAll(specCaptor.capture(), any(Pageable.class));
    Root<Lot> root = mock(Root.class, RETURNS_DEEP_STUBS);
    CriteriaQuery<?> query = mock(CriteriaQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
    specCaptor.getValue().toPredicate(root, query, cb);
  }

  @Test
  void list_appliesEveryFilter_andMapsResults() {
    when(lots.findAll(ArgumentMatchers.<Specification<Lot>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(lot), PageRequest.of(0, 20), 1));

    for (LotStatus state : new LotStatus[]{null, LotStatus.AVAILABLE, LotStatus.EXPIRED, LotStatus.DEPLETED}) {
      PageResponse<LotDto.Response> page = service.list(1L, 2L, state, " jam ", PageRequest.of(0, 20));
      assertEquals(1, page.totalElements());
      assertEquals("L-1", page.content().get(0).lotNumber());
      runLastSpecification();
    }

    service.list(null, null, null, null, PageRequest.of(0, 20));
    runLastSpecification();
  }
}