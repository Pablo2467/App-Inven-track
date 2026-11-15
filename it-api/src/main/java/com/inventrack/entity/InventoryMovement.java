package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "inventory_movements")
@Getter @Setter
public class InventoryMovement extends BaseEntity {
  @Enumerated(EnumType.STRING) @Column(name = "movement_type", nullable = false, length = 10) private MovementType type;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lot_id") private Lot lot;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "entry_id") private StockEntry entry;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "output_id") private StockExit output;
  @Column(nullable = false, precision = 14, scale = 3) private BigDecimal quantity;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
}
