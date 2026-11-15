package com.inventrack.mapper;

import com.inventrack.dto.*;
import com.inventrack.entity.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Mappers {
  private Mappers() {}

  public static ProductDto.Response product(Product p) {
    return new ProductDto.Response(p.getId(), p.getCode(), p.getName(), p.getDescription(), p.getCategory(), p.getUnit(),
        p.getMinStock(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
  }

  public static SupplierDto.Response supplier(Supplier s) {
    return new SupplierDto.Response(s.getId(), s.getName(), s.getTaxId(), s.getPhone(), s.getEmail(), s.getAddress(),
        s.getStatus(), s.getCreatedAt(), s.getUpdatedAt());
  }

  public static UserDto.Response user(User u) {
    return new UserDto.Response(u.getId(), u.getFullName(), u.getEmail(), u.getUsername(), u.getRole(), u.getStatus(),
        u.getCreatedAt(), u.getLastLoginAt());
  }

  public static LotDto.Response lot(Lot l, LocalDate today) {
    return new LotDto.Response(l.getId(), l.getLotNumber(), l.getProduct().getId(), l.getProduct().getCode(), l.getProduct().getName(),
        l.getSupplier().getId(), l.getSupplier().getName(), l.getInitialQuantity(), l.getAvailableQuantity(),
        l.getEntryDate(), l.getExpirationDate(), l.getUnitCost(), l.effectiveStatus(today),
        ChronoUnit.DAYS.between(today, l.getExpirationDate()));
  }

  public static EntryDto.Response entry(StockEntry e) {
    return new EntryDto.Response(e.getId(), e.getProduct().getId(), e.getProduct().getName(), e.getSupplier().getId(),
        e.getSupplier().getName(), e.getLot().getId(), e.getLot().getLotNumber(), e.getQuantity(), e.getEntryDate(),
        e.getLot().getExpirationDate(), e.getUser().getId(), e.getUser().getFullName(), e.getNotes(), e.getCreatedAt());
  }

  public static ExitDto.Response exit(StockExit x, java.util.List<ExitDto.Allocation> allocations) {
    return new ExitDto.Response(x.getId(), x.getProduct().getId(), x.getProduct().getName(), x.getQuantity(), x.getReason(),
        x.getNotes(), x.getExitDate(), x.getUser().getId(), x.getUser().getFullName(), x.getCreatedAt(), allocations);
  }

  public static MovementDto.Response movement(InventoryMovement m) {
    String detail = m.getType() == MovementType.EXIT && m.getOutput() != null
        ? "Salida: " + m.getOutput().getReason().name()
        : "Entrada de proveedor";
    return new MovementDto.Response(m.getId(), m.getType(), m.getProduct().getId(), m.getProduct().getName(),
        m.getLot().getId(), m.getLot().getLotNumber(), m.getQuantity(),
        m.getEntry() == null ? null : m.getEntry().getId(), m.getOutput() == null ? null : m.getOutput().getId(),
        detail, m.getUser().getId(), m.getUser().getFullName(), m.getCreatedAt());
  }
}
