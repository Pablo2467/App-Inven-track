package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "lots")
@Getter @Setter
public class Lot extends BaseEntity {
  @Column(name = "lot_number", nullable = false, length = 80) private String lotNumber;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id") private Supplier supplier;
  @Column(name = "initial_quantity", nullable = false, precision = 14, scale = 3) private BigDecimal initialQuantity;
  @Column(name = "available_quantity", nullable = false, precision = 14, scale = 3) private BigDecimal availableQuantity;
  @Column(name = "entry_date", nullable = false) private LocalDate entryDate;
  @Column(name = "expiration_date", nullable = false) private LocalDate expirationDate;
  @Column(name = "unit_cost", precision = 14, scale = 2) private BigDecimal unitCost;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private LotStatus status = LotStatus.AVAILABLE;

  /** Estado real calculado con la fecha de hoy: agotado, vencido o disponible. */
  public LotStatus effectiveStatus(LocalDate today) {
    if (availableQuantity.signum() <= 0) return LotStatus.DEPLETED;
    if (expirationDate.isBefore(today)) return LotStatus.EXPIRED;
    return LotStatus.AVAILABLE;
  }
}
