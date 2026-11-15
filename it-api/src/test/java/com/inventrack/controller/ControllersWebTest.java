package com.inventrack.controller;

import com.inventrack.config.SecurityConfig;
import com.inventrack.dto.*;
import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.entity.*;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.InsufficientStockException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.repository.UserRepository;
import com.inventrack.security.JsonErrorWriter;
import com.inventrack.security.JwtService;
import com.inventrack.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prueba todos los controladores por HTTP (seguridad real, servicios simulados) y el manejo de errores. */
@WebMvcTest(controllers = {AuthController.class, ProductController.class, SupplierController.class, UserController.class,
    LotController.class, StockEntryController.class, StockExitController.class, ReportController.class})
@Import({SecurityConfig.class, JwtService.class, JsonErrorWriter.class})
@TestPropertySource(properties = {
    "app.jwt.secret=una-clave-de-prueba-con-mas-de-32-caracteres-1234567890",
    "app.jwt.expiration-ms=28800000",
    "app.cors.allowed-origins=http://localhost:4200"})
class ControllersWebTest {
  private static final String AUTH = "Authorization";
  private static final MediaType JSON = MediaType.APPLICATION_JSON;
  private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 10, 0);
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 14);

  @Autowired MockMvc mvc;
  @Autowired JwtService jwt;
  @MockBean UserRepository users;
  @MockBean AuthService authService;
  @MockBean ProductService productService;
  @MockBean SupplierService supplierService;
  @MockBean UserService userService;
  @MockBean LotService lotService;
  @MockBean StockEntryService entryService;
  @MockBean StockExitService exitService;
  @MockBean InventoryService inventoryService;
  @MockBean AlertService alertService;
  @MockBean MovementService movementService;
  @MockBean DashboardService dashboardService;

  String owner;

  @BeforeEach
  void setUp() {
    User u = new User();
    u.setId(1L); u.setUsername("dueno"); u.setRole(Role.DUENO); u.setStatus(Status.ACTIVE);
    when(users.findById(1L)).thenReturn(Optional.of(u));
    owner = "Bearer " + jwt.generate(u);
  }

  // ---------- Auth ----------

  @Test
  void login_isPublic_andReturnsToken() throws Exception {
    when(authService.login(any())).thenReturn(new AuthDto.LoginResponse("tok", "Bearer", 28800L, null));
    mvc.perform(post("/api/v1/auth/login").contentType(JSON).content("{\"username\":\"dueno\",\"password\":\"x\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("tok"));
  }

  @Test
  void login_withBlankFields_is400() throws Exception {
    mvc.perform(post("/api/v1/auth/login").contentType(JSON).content("{\"username\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void me_andChangePassword_needToken() throws Exception {
    when(authService.me(any())).thenReturn(new UserDto.Response(1L, "Dueño", "d@x.co", "dueno", Role.DUENO, Status.ACTIVE, NOW, null));
    mvc.perform(get("/api/v1/auth/me").header(AUTH, owner)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("dueno"));
    mvc.perform(post("/api/v1/auth/change-password").header(AUTH, owner).contentType(JSON)
        .content("{\"currentPassword\":\"actual\",\"newPassword\":\"nueva-clave-1\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("OK"));
    mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  // ---------- Productos ----------

  private static final String PRODUCT_JSON = "{\"code\":\"JAM-001\",\"name\":\"Jamón\",\"unit\":\"kg\",\"minStock\":1}";

  private ProductDto.Response product() {
    return new ProductDto.Response(1L, "JAM-001", "Jamón", null, "Embutidos", "kg", BigDecimal.ONE, Status.ACTIVE, NOW, NOW);
  }

  @Test
  void products_getUpdateDeleteAndList() throws Exception {
    when(productService.get(1L)).thenReturn(product());
    when(productService.update(eq(1L), any())).thenReturn(product());
    when(productService.delete(1L)).thenReturn(new MessageResponse("OK", "Producto eliminado"));

    mvc.perform(get("/api/v1/products").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/products/1").header(AUTH, owner)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("JAM-001"));
    mvc.perform(put("/api/v1/products/1").header(AUTH, owner).contentType(JSON).content(PRODUCT_JSON)).andExpect(status().isOk());
    mvc.perform(delete("/api/v1/products/1").header(AUTH, owner)).andExpect(status().isOk()).andExpect(jsonPath("$.result").value("OK"));
  }

  // ---------- Proveedores ----------

  @Test
  void suppliers_fullCrud() throws Exception {
    SupplierDto.Response s = new SupplierDto.Response(1L, "Distribuciones", "9001", "3001234567", "v@x.co", "Neiva", Status.ACTIVE, NOW, NOW);
    when(supplierService.create(any())).thenReturn(s);
    when(supplierService.get(1L)).thenReturn(s);
    when(supplierService.update(eq(1L), any())).thenReturn(s);
    when(supplierService.delete(1L)).thenReturn(new MessageResponse("OK", "Proveedor eliminado"));
    String body = "{\"name\":\"Distribuciones\",\"taxId\":\"9001\",\"phone\":\"3001234567\",\"email\":\"v@x.co\",\"address\":\"Neiva\"}";

    mvc.perform(post("/api/v1/suppliers").header(AUTH, owner).contentType(JSON).content(body))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.taxId").value("9001"));
    mvc.perform(get("/api/v1/suppliers").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/suppliers/1").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(put("/api/v1/suppliers/1").header(AUTH, owner).contentType(JSON).content(body)).andExpect(status().isOk());
    mvc.perform(delete("/api/v1/suppliers/1").header(AUTH, owner)).andExpect(status().isOk());
  }

  @Test
  void suppliers_invalidEmail_is400_withFieldDetails() throws Exception {
    mvc.perform(post("/api/v1/suppliers").header(AUTH, owner).contentType(JSON)
        .content("{\"name\":\"X\",\"taxId\":\"1\",\"email\":\"no-es-correo\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.email").exists());
  }

  // ---------- Usuarios ----------

  @Test
  void users_fullCrud_andStatusChange() throws Exception {
    UserDto.Response u = new UserDto.Response(5L, "Ana", "ana@x.co", "ana", Role.EMPLEADA, Status.ACTIVE, NOW, null);
    when(userService.create(any())).thenReturn(u);
    when(userService.get(5L)).thenReturn(u);
    when(userService.update(eq(5L), any())).thenReturn(u);
    when(userService.setStatus(eq(5L), eq(Status.INACTIVE), any())).thenReturn(u);

    mvc.perform(post("/api/v1/users").header(AUTH, owner).contentType(JSON)
        .content("{\"fullName\":\"Ana\",\"email\":\"ana@x.co\",\"username\":\"ana\",\"password\":\"clave-de-prueba\",\"role\":\"EMPLEADA\"}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("ana"));
    mvc.perform(get("/api/v1/users").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/users/5").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(put("/api/v1/users/5").header(AUTH, owner).contentType(JSON)
        .content("{\"fullName\":\"Ana\",\"email\":\"ana@x.co\",\"role\":\"EMPLEADA\"}")).andExpect(status().isOk());
    mvc.perform(patch("/api/v1/users/5/status").header(AUTH, owner).contentType(JSON).content("{\"status\":\"INACTIVE\"}"))
        .andExpect(status().isOk());
  }

  // ---------- Lotes ----------

  @Test
  void batches_createListGetUpdate() throws Exception {
    EntryDto.Response entry = new EntryDto.Response(7L, 1L, "Jamón", 2L, "Distribuciones", 10L, "L-1", BigDecimal.TEN,
        TODAY, TODAY.plusDays(30), 1L, "Dueño", null, NOW);
    LotDto.Response lot = new LotDto.Response(10L, "L-1", 1L, "JAM-001", "Jamón", 2L, "Distribuciones", BigDecimal.TEN, BigDecimal.TEN,
        TODAY, TODAY.plusDays(30), null, LotStatus.AVAILABLE, 30);
    when(entryService.register(any(), any())).thenReturn(entry);
    when(lotService.get(10L)).thenReturn(lot);
    when(lotService.update(eq(10L), any())).thenReturn(lot);

    mvc.perform(post("/api/v1/batches").header(AUTH, owner).contentType(JSON).content("{\"productId\":1,\"supplierId\":2,\"quantity\":10}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.lotNumber").value("L-1"));
    mvc.perform(get("/api/v1/batches?status=AVAILABLE&q=jam").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/batches/10").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(put("/api/v1/batches/10").header(AUTH, owner).contentType(JSON)
        .content("{\"lotNumber\":\"L-1\",\"expirationDate\":\"2026-12-14\",\"unitCost\":1500}")).andExpect(status().isOk());
  }

  // ---------- Entradas y salidas ----------

  @Test
  void stockEntries_listGetRegister() throws Exception {
    EntryDto.Response entry = new EntryDto.Response(7L, 1L, "Jamón", 2L, "Distribuciones", 10L, "L-1", BigDecimal.TEN,
        TODAY, TODAY.plusDays(30), 1L, "Dueño", null, NOW);
    when(entryService.get(7L)).thenReturn(entry);
    when(entryService.register(any(), any())).thenReturn(entry);

    mvc.perform(get("/api/v1/stock-entries?from=2026-11-01&to=2026-11-14").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/stock-entries/7").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(post("/api/v1/stock-entries").header(AUTH, owner).contentType(JSON).content("{\"productId\":1,\"supplierId\":2,\"quantity\":5}"))
        .andExpect(status().isCreated());
  }

  private ExitDto.Response exit() {
    return new ExitDto.Response(1L, 1L, "Jamón", BigDecimal.ONE, ExitReason.SALE, null, TODAY, 1L, "Dueño", NOW, List.of());
  }

  @Test
  void stockExits_listGetRegister() throws Exception {
    when(exitService.get(1L)).thenReturn(exit());
    when(exitService.register(any(), any())).thenReturn(exit());

    mvc.perform(get("/api/v1/stock-exits?reason=SALE").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/stock-exits/1").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(post("/api/v1/stock-exits").header(AUTH, owner).contentType(JSON).content("{\"productId\":1,\"quantity\":1,\"reason\":\"SALE\"}"))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.reason").value("SALE"));
  }

  // ---------- Reportes ----------

  @Test
  void reports_inventoryAlertsMovementsAndDashboard() throws Exception {
    mvc.perform(get("/api/v1/inventory?q=jam&days=10").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/expiration-alerts?days=5&type=EXPIRING").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/movements?type=ENTRY&from=2026-11-01&to=2026-11-14").header(AUTH, owner)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/dashboard").header(AUTH, owner)).andExpect(status().isOk());
  }

  // ---------- Manejo de errores (GlobalExceptionHandler) ----------

  @Test
  void validationError_is400_withDetails() throws Exception {
    mvc.perform(post("/api/v1/products").header(AUTH, owner).contentType(JSON).content("{\"code\":\"\",\"name\":\"\",\"unit\":\"\"}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.details.code").exists());
  }

  @Test
  void notFound_is404() throws Exception {
    when(productService.get(99L)).thenThrow(new NotFoundException("Producto no encontrado"));
    mvc.perform(get("/api/v1/products/99").header(AUTH, owner))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Producto no encontrado"));
  }

  @Test
  void conflict_is409() throws Exception {
    when(productService.create(any())).thenThrow(new ConflictException("Ya existe un producto con ese código"));
    mvc.perform(post("/api/v1/products").header(AUTH, owner).contentType(JSON).content(PRODUCT_JSON)).andExpect(status().isConflict());
  }

  @Test
  void badRequestException_is400() throws Exception {
    when(productService.create(any())).thenThrow(new BadRequestException("Dato inválido"));
    mvc.perform(post("/api/v1/products").header(AUTH, owner).contentType(JSON).content(PRODUCT_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void insufficientStock_is409() throws Exception {
    when(exitService.register(any(), any())).thenThrow(new InsufficientStockException("Stock insuficiente"));
    mvc.perform(post("/api/v1/stock-exits").header(AUTH, owner).contentType(JSON).content("{\"productId\":1,\"quantity\":999,\"reason\":\"SALE\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void dataIntegrityViolation_is409() throws Exception {
    when(productService.create(any())).thenThrow(new DataIntegrityViolationException("duplicado"));
    mvc.perform(post("/api/v1/products").header(AUTH, owner).contentType(JSON).content(PRODUCT_JSON)).andExpect(status().isConflict());
  }

  @Test
  void unexpectedError_is500_withFriendlyMessage() throws Exception {
    when(productService.get(1L)).thenThrow(new RuntimeException("boom"));
    mvc.perform(get("/api/v1/products/1").header(AUTH, owner)).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.status").value(500));
  }

  @Test
  void malformedJson_is400() throws Exception {
    mvc.perform(post("/api/v1/products").header(AUTH, owner).contentType(JSON).content("{")).andExpect(status().isBadRequest());
  }

  @Test
  void invalidEnumParameter_is400() throws Exception {
    mvc.perform(get("/api/v1/stock-exits?reason=NO-EXISTE").header(AUTH, owner)).andExpect(status().isBadRequest());
  }

  @Test
  void unsupportedMethod_is405() throws Exception {
    mvc.perform(patch("/api/v1/products/1").header(AUTH, owner).contentType(JSON).content("{}")).andExpect(status().isMethodNotAllowed());
  }

  @Test
  void unknownRoute_is404() throws Exception {
    mvc.perform(get("/api/v1/no-existe").header(AUTH, owner)).andExpect(status().isNotFound());
  }
}