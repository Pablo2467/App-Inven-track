package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.LotDto;
import com.inventrack.entity.Lot;
import com.inventrack.entity.LotStatus;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.LotRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LotService {
  private final LotRepository lots;
  private final Clock clock;

  @Transactional(readOnly = true)
  public PageResponse<LotDto.Response> list(Long productId, Long supplierId, LotStatus state, String q, Pageable pageable) {
    LocalDate today = LocalDate.now(clock);
    Specification<Lot> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (productId != null) ps.add(cb.equal(root.get("product").get("id"), productId));
      if (supplierId != null) ps.add(cb.equal(root.get("supplier").get("id"), supplierId));
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        ps.add(cb.or(cb.like(cb.lower(root.get("lotNumber")), like), cb.like(cb.lower(root.get("product").get("name")), like)));
      }
      if (state != null) {
        Predicate hasStock = cb.greaterThan(root.<BigDecimal>get("availableQuantity"), BigDecimal.ZERO);
        switch (state) {
          case DEPLETED -> ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("availableQuantity"), BigDecimal.ZERO));
          case EXPIRED -> { ps.add(hasStock); ps.add(cb.lessThan(root.<LocalDate>get("expirationDate"), today)); }
          case AVAILABLE -> { ps.add(hasStock); ps.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("expirationDate"), today)); }
        }
      }
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(lots.findAll(spec, pageable), l -> Mappers.lot(l, today));
  }

  @Transactional(readOnly = true)
  public LotDto.Response get(Long id) { return Mappers.lot(find(id), LocalDate.now(clock)); }

  @Transactional
  public LotDto.Response update(Long id, LotDto.UpdateRequest r) {
    Lot l = find(id);
    String number = r.lotNumber().trim();
    if (lots.existsByProductIdAndLotNumberIgnoreCaseAndIdNot(l.getProduct().getId(), number, id))
      throw new ConflictException("Ya existe otro lote con ese número para este producto");
    if (r.expirationDate().isBefore(l.getEntryDate())) throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de ingreso");
    l.setLotNumber(number);
    l.setExpirationDate(r.expirationDate());
    l.setUnitCost(r.unitCost());
    return Mappers.lot(lots.save(l), LocalDate.now(clock));
  }

  private Lot find(Long id) { return lots.findById(id).orElseThrow(() -> new NotFoundException("Lote no encontrado")); }
}
