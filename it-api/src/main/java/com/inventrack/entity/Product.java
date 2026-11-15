package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "products")
@Getter @Setter
public class Product extends BaseEntity {
  @Column(nullable = false, length = 40) private String code;
  @Column(nullable = false, length = 150) private String name;
  @Column(length = 500) private String description;
  @Column(length = 100) private String category;
  @Column(nullable = false, length = 30) private String unit;
  @Column(name = "min_stock", nullable = false, precision = 14, scale = 3) private BigDecimal minStock = BigDecimal.ZERO;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.ACTIVE;
}
