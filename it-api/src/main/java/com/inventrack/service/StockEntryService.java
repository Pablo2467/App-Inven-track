package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.EntryDto;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.exception.UnauthorizedException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.*;
import com.inventrack.security.AuthenticatedUser;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockEntryService {
  private final StockEntryRepository entries;
  private final LotRepository lots;
  private final ProductRepository products;
  private final SupplierRepository suppliers;
  private final UserRepository users;
  private final InventoryMovementRepository movements;
  private final Clock clock;
  private final int defaultExpirationDays;

  public StockEntryService(StockEntryRepository entries, LotRepository lots, ProductRepository products, SupplierRepository suppliers,
                           UserRepository users, InventoryMovementRepository movements, Clock clock,
                           @Value("${app.lot.default-expiration-days:30}") int defaultExpirationDays) {
    this.entries = entries; this.lots = lots; this.products = products; this.suppliers = suppliers;
    this.users = users; this.movements = movements; this.clock = clock; this.defaultExpirationDays = defaultExpirationDays;
  }

  /** Registra la entrada, crea el lote y deja el movimiento, todo en una sola transacción. */
  @Transactional
  public EntryDto.Response register(EntryDto.Request r, AuthenticatedUser auth) {
    LocalDate today = LocalDate.now(clock);
    User user = users.findById(auth.id()).orElseThrow(() -> new UnauthorizedException("Sesión inválida"));
    Product product = products.findById(r.productId()).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    Supplier supplier = suppliers.findById(r.supplierId()).orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
    if (product.getStatus() != Status.ACTIVE) throw new BadRequestException("El producto está inactivo");
    if (supplier.getStatus() != Status.ACTIVE) throw new BadRequestException("El proveedor está inactivo");

    LocalDate entryDate = r.entryDate() == null ? today : r.entryDate();
    if (entryDate.isAfter(today)) throw new BadRequestException("La fecha de ingreso no puede ser futura");
    LocalDate expiration = r.expirationDate() == null ? entryDate.plusDays(defaultExpirationDays) : r.expirationDate();
    if (expiration.isBefore(entryDate)) throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de ingreso");

    String lotNumber = (r.lotNumber() == null || r.lotNumber().isBlank())
        ? "LOT-" + product.getCode() + "-" + LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        : r.lotNumber().trim();
    if (lots.existsByProductIdAndLotNumberIgnoreCase(product.getId(), lotNumber))
      throw new ConflictException("Ya existe el lote " + lotNumber + " para este producto");

    Lot lot = new Lot();
    lot.setLotNumber(lotNumber);
    lot.setProduct(product);
    lot.setSupplier(supplier);
    lot.setInitialQuantity(r.quantity());
    lot.setAvailableQuantity(r.quantity());
    lot.setEntryDate(entryDate);
    lot.setExpirationDate(expiration);
    lot.setUnitCost(r.unitCost());
    lot.setStatus(LotStatus.AVAILABLE);
    lot = lots.save(lot);

    StockEntry entry = new StockEntry();
    entry.setProduct(product);
    entry.setSupplier(supplier);
    entry.setLot(lot);
    entry.setQuantity(r.quantity());
    entry.setEntryDate(entryDate);
    entry.setUser(user);
    entry.setNotes(ProductService.blankToNull(r.notes()));
    entry = entries.save(entry);

    InventoryMovement m = new InventoryMovement();
    m.setType(MovementType.ENTRY);
    m.setProduct(product);
    m.setLot(lot);
    m.setEntry(entry);
    m.setQuantity(r.quantity());
    m.setUser(user);
    movements.save(m);

    return Mappers.entry(entry);
  }

  @Transactional(readOnly = true)
  public PageResponse<EntryDto.Response> list(Long productId, Long supplierId, LocalDate from, LocalDate to, Pageable pageable) {
    Specification<StockEntry> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (productId != null) ps.add(cb.equal(root.get("product").get("id"), productId));
      if (supplierId != null) ps.add(cb.equal(root.get("supplier").get("id"), supplierId));
      if (from != null) ps.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("entryDate"), from));
      if (to != null) ps.add(cb.lessThanOrEqualTo(root.<LocalDate>get("entryDate"), to));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(entries.findAll(spec, pageable), Mappers::entry);
  }

  @Transactional(readOnly = true)
  public EntryDto.Response get(Long id) {
    return Mappers.entry(entries.findById(id).orElseThrow(() -> new NotFoundException("Entrada no encontrada")));
  }
}
