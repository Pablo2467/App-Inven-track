package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "suppliers")
@Getter @Setter
public class Supplier extends BaseEntity {
  @Column(nullable = false, length = 150) private String name;
  @Column(name = "tax_id", nullable = false, length = 40) private String taxId;
  @Column(length = 40) private String phone;
  @Column(length = 150) private String email;
  @Column(length = 255) private String address;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.ACTIVE;
}
