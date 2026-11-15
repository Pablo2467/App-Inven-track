package com.inventrack.service;

import com.inventrack.dto.UserDto;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.repository.UserRepository;
import com.inventrack.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
  UserRepository users;
  PasswordEncoder encoder;
  UserService service;

  @BeforeEach
  void setUp() {
    users = mock(UserRepository.class);
    encoder = new BCryptPasswordEncoder();
    service = new UserService(users, encoder);
    when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
  }

  private UserDto.CreateRequest create(String username, String email) {
    return new UserDto.CreateRequest("Laura Gómez", email, username, "Clave12345*", Role.EMPLEADA);
  }

  @Test
  void create_storesPasswordWithBCrypt_neverPlainText() {
    service.create(create("laura", "laura@test.com"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(users).save(captor.capture());
    String hash = captor.getValue().getPasswordHash();
    assertNotEquals("Clave12345*", hash);
    assertTrue(hash.startsWith("$2"), "Debe ser un hash BCrypt");
    assertTrue(encoder.matches("Clave12345*", hash));
  }

  @Test
  void response_neverExposesPassword() {
    boolean exposes = Arrays.stream(UserDto.Response.class.getRecordComponents()).anyMatch(c -> c.getName().toLowerCase().contains("password"));
    assertFalse(exposes);
    UserDto.Response r = service.create(create("laura", "laura@test.com"));
    assertEquals(Role.EMPLEADA, r.role());
    assertEquals(Status.ACTIVE, r.status());
  }

  @Test
  void create_duplicateUsername_throwsConflict() {
    when(users.existsByUsernameIgnoreCase("laura")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(create("laura", "laura@test.com")));
  }

  @Test
  void create_duplicateEmail_throwsConflict() {
    when(users.existsByEmailIgnoreCase("laura@test.com")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(create("laura", "laura@test.com")));
  }

  @Test
  void update_blankPassword_keepsExistingHash() {
    User u = TestData.user(2L, "laura", Role.EMPLEADA);
    u.setPasswordHash("hash-original");
    when(users.findById(2L)).thenReturn(Optional.of(u));
    service.update(2L, new UserDto.UpdateRequest("Laura G.", "laura@test.com", Role.EMPLEADA, ""));
    assertEquals("hash-original", u.getPasswordHash());
    assertEquals("Laura G.", u.getFullName());
  }

  @Test
  void update_newPassword_isReEncoded() {
    User u = TestData.user(2L, "laura", Role.EMPLEADA);
    when(users.findById(2L)).thenReturn(Optional.of(u));
    service.update(2L, new UserDto.UpdateRequest("Laura", "laura@test.com", Role.EMPLEADA, "OtraClave123"));
    assertTrue(encoder.matches("OtraClave123", u.getPasswordHash()));
  }

  @Test
  void setStatus_deactivatesAndActivates() {
    User u = TestData.user(2L, "laura", Role.EMPLEADA);
    when(users.findById(2L)).thenReturn(Optional.of(u));
    AuthenticatedUser actor = new AuthenticatedUser(1L, "dueno", Role.DUENO);
    assertEquals(Status.INACTIVE, service.setStatus(2L, Status.INACTIVE, actor).status());
    assertEquals(Status.ACTIVE, service.setStatus(2L, Status.ACTIVE, actor).status());
  }

  @Test
  void setStatus_cannotDeactivateYourself() {
    User u = TestData.user(1L, "dueno", Role.DUENO);
    when(users.findById(1L)).thenReturn(Optional.of(u));
    assertThrows(BadRequestException.class, () -> service.setStatus(1L, Status.INACTIVE, new AuthenticatedUser(1L, "dueno", Role.DUENO)));
  }

  @Test
  void setStatus_cannotDeactivateLastActiveOwner() {
    User owner = TestData.user(3L, "otro", Role.DUENO);
    when(users.findById(3L)).thenReturn(Optional.of(owner));
    when(users.countByRoleAndStatus(Role.DUENO, Status.ACTIVE)).thenReturn(1L);
    assertThrows(BadRequestException.class, () -> service.setStatus(3L, Status.INACTIVE, new AuthenticatedUser(1L, "dueno", Role.DUENO)));
  }
}
