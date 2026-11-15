package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.MovementDto;
import com.inventrack.entity.InventoryMovement;
import com.inventrack.entity.MovementType;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.InventoryMovementRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovementService {
  private final InventoryMovementRepository movements;

  @Transactional(readOnly = true)
  public PageResponse<MovementDto.Response> list(MovementType type, Long productId, Long userId, LocalDate from, LocalDate to, Pageable pageable) {
    Specification<InventoryMovement> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (type != null) ps.add(cb.equal(root.get("type"), type));
      if (productId != null) ps.add(cb.equal(root.get("product").get("id"), productId));
      if (userId != null) ps.add(cb.equal(root.get("user").get("id"), userId));
      if (from != null) ps.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), from.atStartOfDay()));
      if (to != null) ps.add(cb.lessThan(root.<LocalDateTime>get("createdAt"), to.plusDays(1).atStartOfDay()));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(movements.findAll(spec, pageable), Mappers::movement);
  }
}
