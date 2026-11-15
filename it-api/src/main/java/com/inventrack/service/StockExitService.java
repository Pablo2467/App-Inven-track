package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.ExitDto;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.InsufficientStockException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.exception.UnauthorizedException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.*;
import com.inventrack.security.AuthenticatedUser;
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
public class StockExitService {
  private final StockExitRepository exits;
  private final LotRepository lots;
  private final ProductRepository products;
  private final UserRepository users;
  private final InventoryMovementRepository movements;
  private final Clock clock;

  /**
   * Salida con FIFO: descuenta primero los lotes más antiguos (por fecha de ingreso) que aún tengan existencias.
   * Los lotes se bloquean (SELECT ... FOR UPDATE) dentro de la transacción, así dos usuarios no descuentan las mismas unidades.
   * Si algo falla, se revierte todo. Las salidas normales no usan lotes vencidos; EXPIRED_DISPOSAL da de baja solo lotes vencidos.
   */
  @Transactional
  public ExitDto.Response register(ExitDto.Request r, AuthenticatedUser auth) {
    LocalDate today = LocalDate.now(clock);
    User user = users.findById(auth.id()).orElseThrow(() -> new UnauthorizedException("Sesión inválida"));
    Product product = products.findById(r.productId()).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    LocalDate exitDate = r.exitDate() == null ? today : r.exitDate();
    if (exitDate.isAfter(today)) throw new BadRequestException("La fecha de salida no puede ser futura");

    boolean disposal = r.reason() == ExitReason.EXPIRED_DISPOSAL;
    List<Lot> candidates = disposal ? lots.lockExpiredLots(product.getId(), today) : lots.lockUsableLots(product.getId(), today);
    BigDecimal available = candidates.stream().map(Lot::getAvailableQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (available.compareTo(r.quantity()) < 0) {
      throw new InsufficientStockException("Existencias insuficientes de " + product.getName() + ": solicitaste "
          + r.quantity().stripTrailingZeros().toPlainString() + " " + product.getUnit() + " y solo hay "
          + available.stripTrailingZeros().toPlainString() + " " + product.getUnit() + (disposal ? " vencidas" : " disponibles (sin contar lotes vencidos)"));
    }

    StockExit exit = new StockExit();
    exit.setProduct(product);
    exit.setQuantity(r.quantity());
    exit.setReason(r.reason());
    exit.setNotes(ProductService.blankToNull(r.notes()));
    exit.setExitDate(exitDate);
    exit.setUser(user);
    exit = exits.save(exit);

    BigDecimal remaining = r.quantity();
    List<ExitDto.Allocation> allocations = new ArrayList<>();
    for (Lot lot : candidates) {
      if (remaining.signum() <= 0) break;
      BigDecimal take = lot.getAvailableQuantity().min(remaining);
      lot.setAvailableQuantity(lot.getAvailableQuantity().subtract(take));
      if (lot.getAvailableQuantity().signum() == 0) lot.setStatus(LotStatus.DEPLETED);
      lots.save(lot);
      remaining = remaining.subtract(take);

      InventoryMovement m = new InventoryMovement();
      m.setType(MovementType.EXIT);
      m.setProduct(product);
      m.setLot(lot);
      m.setOutput(exit);
      m.setQuantity(take);
      m.setUser(user);
      movements.save(m);
      allocations.add(new ExitDto.Allocation(lot.getId(), lot.getLotNumber(), lot.getEntryDate(), lot.getExpirationDate(), take));
    }
    return Mappers.exit(exit, allocations);
  }

  @Transactional(readOnly = true)
  public PageResponse<ExitDto.Response> list(Long productId, ExitReason reason, LocalDate from, LocalDate to, Pageable pageable) {
    Specification<StockExit> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (productId != null) ps.add(cb.equal(root.get("product").get("id"), productId));
      if (reason != null) ps.add(cb.equal(root.get("reason"), reason));
      if (from != null) ps.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("exitDate"), from));
      if (to != null) ps.add(cb.lessThanOrEqualTo(root.<LocalDate>get("exitDate"), to));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(exits.findAll(spec, pageable), x -> Mappers.exit(x, null));
  }

  @Transactional(readOnly = true)
  public ExitDto.Response get(Long id) {
    StockExit x = exits.findById(id).orElseThrow(() -> new NotFoundException("Salida no encontrada"));
    List<ExitDto.Allocation> allocations = movements.findByOutputIdOrderByIdAsc(id).stream()
        .map(m -> new ExitDto.Allocation(m.getLot().getId(), m.getLot().getLotNumber(), m.getLot().getEntryDate(), m.getLot().getExpirationDate(), m.getQuantity()))
        .toList();
    return Mappers.exit(x, allocations);
  }
}
