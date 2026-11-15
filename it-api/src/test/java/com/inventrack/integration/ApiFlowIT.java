package com.inventrack.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBA DE INTEGRACIÓN REAL contra MySQL (usa application.properties).
 * Requiere MySQL encendido y los usuarios DEMO (dueno / empleada) con sus claves iniciales.
 * Ejecutar con:  mvn test -Dtest=ApiFlowIT
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiFlowIT {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  static String dueno, empleada;
  static long productId, supplierId;
  static final String CODE = "IT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

  private String json(Object o) throws Exception { return mapper.writeValueAsString(o); }

  private String login(String user, String pass) throws Exception {
    MvcResult r = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json(Map.of("username", user, "password", pass))))
        .andExpect(status().isOk()).andReturn();
    return "Bearer " + JsonPath.read(r.getResponse().getContentAsString(), "$.token");
  }

  @Test @Order(1)
  void login_rejectsWrongPassword_andAcceptsValidOnes() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json(Map.of("username", "dueno", "password", "incorrecta"))))
        .andExpect(status().isUnauthorized());
    dueno = login("dueno", "Dueno123*");
    empleada = login("empleada", "Empleada123*");
  }

  @Test @Order(2)
  void protectedEndpoint_requiresToken() throws Exception {
    mvc.perform(get("/api/v1/products")).andExpect(status().isUnauthorized());
  }

  @Test @Order(3)
  void dueno_createsProductAndSupplier_persistedInMySql() throws Exception {
    MvcResult p = mvc.perform(post("/api/v1/products").header("Authorization", dueno).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("code", CODE, "name", "Producto de prueba IT", "unit", "kg", "minStock", 2))))
        .andExpect(status().isCreated()).andReturn();
    productId = ((Number) JsonPath.read(p.getResponse().getContentAsString(), "$.id")).longValue();

    MvcResult s = mvc.perform(post("/api/v1/suppliers").header("Authorization", dueno).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", "Proveedor IT " + CODE, "taxId", "IT-" + CODE))))
        .andExpect(status().isCreated()).andReturn();
    supplierId = ((Number) JsonPath.read(s.getResponse().getContentAsString(), "$.id")).longValue();

    mvc.perform(post("/api/v1/products").header("Authorization", dueno).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("code", CODE.toLowerCase(), "name", "Duplicado", "unit", "kg"))))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/v1/products/" + productId).header("Authorization", empleada)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(CODE));
  }

  @Test @Order(4)
  void empleada_cannotCreateProducts_orManageUsers() throws Exception {
    mvc.perform(post("/api/v1/products").header("Authorization", empleada).contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("code", "X-" + CODE, "name", "No permitido", "unit", "kg")))).andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/users").header("Authorization", empleada)).andExpect(status().isForbidden());
  }

  @Test @Order(5)
  void users_endpoint_neverExposesPasswords() throws Exception {
    MvcResult r = mvc.perform(get("/api/v1/users").header("Authorization", dueno)).andExpect(status().isOk()).andReturn();
    String body = r.getResponse().getContentAsString().toLowerCase();
    assertFalse(body.contains("password"));
    assertFalse(body.contains("$2a$"));
  }

  @Test @Order(6)
  void fifo_throughTheApi_withTwoLots() throws Exception {
    String today = java.time.LocalDate.now().toString();
    String old = java.time.LocalDate.now().minusDays(6).toString();
    mvc.perform(post("/api/v1/stock-entries").header("Authorization", empleada).contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("productId", productId, "supplierId", supplierId, "lotNumber", "A-" + CODE, "quantity", 5, "entryDate", old))))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/v1/stock-entries").header("Authorization", empleada).contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("productId", productId, "supplierId", supplierId, "lotNumber", "B-" + CODE, "quantity", 10, "entryDate", today))))
        .andExpect(status().isCreated());

    MvcResult r = mvc.perform(post("/api/v1/stock-exits").header("Authorization", empleada).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("productId", productId, "quantity", 8, "reason", "SALE"))))
        .andExpect(status().isCreated()).andReturn();
    String body = r.getResponse().getContentAsString();
    assertEquals("A-" + CODE, JsonPath.read(body, "$.allocations[0].lotNumber"));
    assertEquals(5.0, ((Number) JsonPath.read(body, "$.allocations[0].quantity")).doubleValue());
    assertEquals("B-" + CODE, JsonPath.read(body, "$.allocations[1].lotNumber"));
    assertEquals(3.0, ((Number) JsonPath.read(body, "$.allocations[1].quantity")).doubleValue());
  }

  @Test @Order(7)
  void exit_exceedingStock_isRejectedWith409() throws Exception {
    mvc.perform(post("/api/v1/stock-exits").header("Authorization", empleada).contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("productId", productId, "quantity", 999, "reason", "SALE"))))
        .andExpect(status().isConflict());
  }

  @Test @Order(8)
  void dashboard_alertsAndMovements_respond() throws Exception {
    mvc.perform(get("/api/v1/dashboard").header("Authorization", empleada)).andExpect(status().isOk()).andExpect(jsonPath("$.totalProducts").exists());
    mvc.perform(get("/api/v1/expiration-alerts").header("Authorization", empleada)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/movements").param("productId", String.valueOf(productId)).header("Authorization", empleada))
        .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
  }

  @Test @Order(9)
  void deleteProductWithHistory_deactivatesIt() throws Exception {
    mvc.perform(delete("/api/v1/products/" + productId).header("Authorization", dueno))
        .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("DEACTIVATED"));
    mvc.perform(get("/api/v1/products/" + productId).header("Authorization", dueno)).andExpect(jsonPath("$.status").value("INACTIVE"));
  }
}
