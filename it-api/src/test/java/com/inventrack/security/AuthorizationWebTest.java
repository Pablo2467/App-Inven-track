package com.inventrack.security;

import com.inventrack.config.SecurityConfig;
import com.inventrack.controller.ProductController;
import com.inventrack.controller.StockExitController;
import com.inventrack.controller.UserController;
import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.ExitDto;
import com.inventrack.dto.ProductDto;
import com.inventrack.entity.ExitReason;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.repository.UserRepository;
import com.inventrack.service.ProductService;
import com.inventrack.service.StockExitService;
import com.inventrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prueba reglas de acceso por rol (DUEÑO / EMPLEADA) sin necesidad de MySQL. */
@WebMvcTest(controllers = {ProductController.class, UserController.class, StockExitController.class})
@Import({SecurityConfig.class, JwtService.class, JsonErrorWriter.class})
@TestPropertySource(properties = {
    "app.jwt.secret=una-clave-de-prueba-con-mas-de-32-caracteres-1234567890",
    "app.jwt.expiration-ms=28800000",
    "app.cors.allowed-origins=http://localhost:3000"})
class AuthorizationWebTest {
  @Autowired MockMvc mvc;
  @Autowired JwtService jwt;
  @MockBean UserRepository users;
  @MockBean ProductService productService;
  @MockBean UserService userService;
  @MockBean StockExitService exitService;

  String duenoToken, empleadaToken;

  private User user(Long id, String username, Role role) {
    User u = new User();
    u.setId(id); u.setUsername(username); u.setRole(role); u.setStatus(Status.ACTIVE);
    return u;
  }

  @BeforeEach
  void setUp() {
    User dueno = user(1L, "dueno", Role.DUENO), empleada = user(2L, "empleada", Role.EMPLEADA);
    when(users.findById(1L)).thenReturn(Optional.of(dueno));
    when(users.findById(2L)).thenReturn(Optional.of(empleada));
    duenoToken = "Bearer " + jwt.generate(dueno);
    empleadaToken = "Bearer " + jwt.generate(empleada);

    ProductDto.Response p = new ProductDto.Response(1L, "JAM-001", "Jamón", null, "Embutidos", "kg", BigDecimal.ONE, Status.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    when(productService.create(any())).thenReturn(p);
    when(productService.list(any(), any(), any())).thenReturn(new PageResponse<>(List.of(p), 0, 20, 1, 1));
    when(userService.list(any(), any(), any(), any())).thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
    when(exitService.register(any(), any())).thenReturn(new ExitDto.Response(1L, 1L, "Jamón", BigDecimal.ONE, ExitReason.SALE, null,
        java.time.LocalDate.now(), 2L, "Empleada", LocalDateTime.now(), List.of()));
  }

  private static final String PRODUCT_JSON = "{\"code\":\"JAM-001\",\"name\":\"Jamón\",\"unit\":\"kg\",\"minStock\":1}";
  private static final String EXIT_JSON = "{\"productId\":1,\"quantity\":1,\"reason\":\"SALE\"}";

  @Test
  void withoutToken_isUnauthorized_401() throws Exception {
    mvc.perform(get("/api/v1/products")).andExpect(status().isUnauthorized());
  }

  @Test
  void invalidToken_isUnauthorized_401() throws Exception {
    mvc.perform(get("/api/v1/products").header("Authorization", "Bearer basura")).andExpect(status().isUnauthorized());
  }

  @Test
  void empleada_canReadProducts() throws Exception {
    mvc.perform(get("/api/v1/products").header("Authorization", empleadaToken)).andExpect(status().isOk());
  }

  @Test
  void empleada_cannotCreateProducts_403() throws Exception {
    mvc.perform(post("/api/v1/products").header("Authorization", empleadaToken).contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void dueno_canCreateProducts_201() throws Exception {
    mvc.perform(post("/api/v1/products").header("Authorization", duenoToken).contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("JAM-001"));
  }

  @Test
  void empleada_cannotManageUsers_403() throws Exception {
    mvc.perform(get("/api/v1/users").header("Authorization", empleadaToken)).andExpect(status().isForbidden());
  }

  @Test
  void dueno_canListUsers() throws Exception {
    mvc.perform(get("/api/v1/users").header("Authorization", duenoToken)).andExpect(status().isOk());
  }

  @Test
  void empleada_canRegisterExits() throws Exception {
    mvc.perform(post("/api/v1/stock-exits").header("Authorization", empleadaToken).contentType(MediaType.APPLICATION_JSON).content(EXIT_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  void invalidBody_returns400_withFieldDetails() throws Exception {
    mvc.perform(post("/api/v1/products").header("Authorization", duenoToken).contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"\",\"name\":\"\",\"unit\":\"\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.code").exists());
  }

  @Test
  void xUserIdNotMatchingToken_is403() throws Exception {
    mvc.perform(get("/api/v1/products").header("Authorization", empleadaToken).header("X-User-Id", "1")).andExpect(status().isForbidden());
  }

  @Test
  void xUserIdMatchingToken_isAccepted() throws Exception {
    mvc.perform(get("/api/v1/products").header("Authorization", empleadaToken).header("X-User-Id", "2")).andExpect(status().isOk());
  }
}
