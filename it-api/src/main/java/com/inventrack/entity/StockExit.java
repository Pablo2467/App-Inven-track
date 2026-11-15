package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "outputs")
@Getter @Setter
public class StockExit extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id") private Product product;
  @Column(nullable = false, precision = 14, scale = 3) private BigDecimal quantity;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ExitReason reason;
  @Column(length = 500) private String notes;
  @Column(name = "exit_date", nullable = false) private LocalDate exitDate;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
}
