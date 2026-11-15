package com.inventrack.service;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.UserDto;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.entity.User;
import com.inventrack.exception.BadRequestException;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.UserRepository;
import com.inventrack.security.AuthenticatedUser;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  @Transactional(readOnly = true)
  public PageResponse<UserDto.Response> list(String q, Role role, Status status, Pageable pageable) {
    Specification<User> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        ps.add(cb.or(cb.like(cb.lower(root.get("fullName")), like), cb.like(cb.lower(root.get("username")), like), cb.like(cb.lower(root.get("email")), like)));
      }
      if (role != null) ps.add(cb.equal(root.get("role"), role));
      if (status != null) ps.add(cb.equal(root.get("status"), status));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(users.findAll(spec, pageable), Mappers::user);
  }

  @Transactional(readOnly = true)
  public UserDto.Response get(Long id) { return Mappers.user(find(id)); }

  @Transactional
  public UserDto.Response create(UserDto.CreateRequest r) {
    String username = r.username().trim();
    String email = r.email().trim().toLowerCase();
    if (users.existsByUsernameIgnoreCase(username)) throw new ConflictException("El nombre de usuario ya está en uso");
    if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("El correo electrónico ya está registrado");
    User u = new User();
    u.setFullName(r.fullName().trim());
    u.setEmail(email);
    u.setUsername(username);
    u.setPasswordHash(encoder.encode(r.password()));
    u.setRole(r.role());
    u.setStatus(Status.ACTIVE);
    return Mappers.user(users.save(u));
  }

  @Transactional
  public UserDto.Response update(Long id, UserDto.UpdateRequest r) {
    User u = find(id);
    String email = r.email().trim().toLowerCase();
    if (users.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new ConflictException("El correo electrónico ya está registrado");
    if (u.getRole() == Role.DUENO && r.role() != Role.DUENO) ensureAnotherActiveOwner(u);
    u.setFullName(r.fullName().trim());
    u.setEmail(email);
    u.setRole(r.role());
    if (r.password() != null && !r.password().isBlank()) {
      if (r.password().length() < 8) throw new BadRequestException("La contraseña debe tener al menos 8 caracteres");
      u.setPasswordHash(encoder.encode(r.password()));
    }
    return Mappers.user(users.save(u));
  }

  @Transactional
  public UserDto.Response setStatus(Long id, Status status, AuthenticatedUser actor) {
    User u = find(id);
    if (status == Status.INACTIVE) {
      if (actor != null && actor.id().equals(id)) throw new BadRequestException("No puedes desactivar tu propia cuenta");
      if (u.getRole() == Role.DUENO && u.getStatus() == Status.ACTIVE) ensureAnotherActiveOwner(u);
    }
    u.setStatus(status);
    return Mappers.user(users.save(u));
  }

  private void ensureAnotherActiveOwner(User u) {
    if (u.getStatus() == Status.ACTIVE && users.countByRoleAndStatus(Role.DUENO, Status.ACTIVE) <= 1)
      throw new BadRequestException("Debe existir al menos un usuario DUEÑO activo");
  }

  private User find(Long id) { return users.findById(id).orElseThrow(() -> new NotFoundException("Usuario no encontrado")); }
}
