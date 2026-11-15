package com.inventrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {
  JwtService jwt = new JwtService("una-clave-de-prueba-con-mas-de-32-caracteres-1234567890", 8L * 3600 * 1000);
  UserRepository users = mock(UserRepository.class);
  JwtAuthFilter filter = new JwtAuthFilter(jwt, users, new JsonErrorWriter(new ObjectMapper().findAndRegisterModules()));
  User empleada;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    empleada = new User();
    empleada.setId(5L); empleada.setUsername("empleada"); empleada.setRole(Role.EMPLEADA); empleada.setStatus(Status.ACTIVE);
    when(users.findById(5L)).thenReturn(Optional.of(empleada));
  }

  @AfterEach
  void tearDown() { SecurityContextHolder.clearContext(); }

  private MockHttpServletResponse run(MockHttpServletRequest req) throws Exception {
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, new MockFilterChain());
    return res;
  }

  private MockHttpServletRequest request(String token) {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/products");
    if (token != null) req.addHeader("Authorization", "Bearer " + token);
    return req;
  }

  @Test
  void validToken_setsAuthenticatedUserWithRole() throws Exception {
    run(request(jwt.generate(empleada)));
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(a);
    AuthenticatedUser p = (AuthenticatedUser) a.getPrincipal();
    assertEquals(5L, p.id());
    assertEquals(Role.EMPLEADA, p.role());
    assertTrue(a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_EMPLEADA")));
  }

  @Test
  void noToken_leavesRequestUnauthenticated() throws Exception {
    run(request(null));
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void invalidToken_leavesRequestUnauthenticated() throws Exception {
    run(request("token.invalido.xxx"));
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void deactivatedUser_tokenNoLongerWorks() throws Exception {
    String token = jwt.generate(empleada);
    empleada.setStatus(Status.INACTIVE);
    run(request(token));
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void roleIsTakenFromDatabase_notFromToken() throws Exception {
    String token = jwt.generate(empleada);
    empleada.setRole(Role.DUENO);
    run(request(token));
    assertEquals(Role.DUENO, ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).role());
  }

  @Test
  void xUserId_matchingToken_isAccepted() throws Exception {
    MockHttpServletRequest req = request(jwt.generate(empleada));
    req.addHeader("X-User-Id", "5");
    assertEquals(200, run(req).getStatus());
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void xUserId_differentFromToken_isForbidden() throws Exception {
    MockHttpServletRequest req = request(jwt.generate(empleada));
    req.addHeader("X-User-Id", "1");
    MockHttpServletResponse res = run(req);
    assertEquals(403, res.getStatus());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
