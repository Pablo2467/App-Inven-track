package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "entries")
@Getter @Setter
public class StockEntry extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id") private Supplier supplier;
  @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lot_id") private Lot lot;
  @Column(nullable = false, precision = 14, scale = 3) private BigDecimal quantity;
  @Column(name = "entry_date", nullable = false) private LocalDate entryDate;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
  @Column(length = 500) private String notes;
}
