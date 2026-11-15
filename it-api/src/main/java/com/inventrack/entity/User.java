package com.inventrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "users")
@Getter @Setter
public class User extends BaseEntity {
  @Column(name = "full_name", nullable = false, length = 150) private String fullName;
  @Column(nullable = false, length = 150) private String email;
  @Column(nullable = false, length = 60) private String username;
  @Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.ACTIVE;
  @Column(name = "last_login_at") private LocalDateTime lastLoginAt;
}
