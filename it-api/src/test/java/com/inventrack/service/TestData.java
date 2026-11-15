package com.inventrack.service;

import com.inventrack.entity.*;
import com.inventrack.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Datos y utilidades compartidas por las pruebas unitarias. Fecha fija: 2026-11-14 (hora de Bogotá). */
final class TestData {
  private TestData() {}

  static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-14T15:00:00Z"), ZoneId.of("America/Bogota"));
  static final LocalDate TODAY = LocalDate.of(2026, 11, 14);
  static final AuthenticatedUser ACTOR = new AuthenticatedUser(1L, "dueno", Role.DUENO);

  static User user(Long id, String username, Role role) {
    User u = new User();
    u.setId(id); u.setUsername(username); u.setFullName("Usuario " + username); u.setEmail(username + "@test.local");
    u.setPasswordHash("x"); u.setRole(role); u.setStatus(Status.ACTIVE);
    return u;
  }

  static Product product(Long id, String code) {
    Product p = new Product();
    p.setId(id); p.setCode(code); p.setName("Producto " + code); p.setUnit("kg"); p.setMinStock(new BigDecimal("5")); p.setStatus(Status.ACTIVE);
    return p;
  }

  static Supplier supplier(Long id) {
    Supplier s = new Supplier();
    s.setId(id); s.setName("Proveedor " + id); s.setTaxId("900" + id); s.setStatus(Status.ACTIVE);
    return s;
  }

  static Lot lot(Long id, Product p, Supplier s, String number, String available, LocalDate entry, LocalDate expiration) {
    Lot l = new Lot();
    l.setId(id); l.setLotNumber(number); l.setProduct(p); l.setSupplier(s);
    l.setInitialQuantity(new BigDecimal(available)); l.setAvailableQuantity(new BigDecimal(available));
    l.setEntryDate(entry); l.setExpirationDate(expiration); l.setStatus(LotStatus.AVAILABLE);
    return l;
  }
}
