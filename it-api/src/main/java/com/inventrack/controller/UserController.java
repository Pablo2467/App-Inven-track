package com.inventrack.controller;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.UserDto;
import com.inventrack.entity.Role;
import com.inventrack.entity.Status;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService service;

  @GetMapping
  public PageResponse<UserDto.Response> list(@RequestParam(required = false) String q, @RequestParam(required = false) Role role,
                                             @RequestParam(required = false) Status status,
                                             @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
    return service.list(q, role, status, pageable);
  }

  @GetMapping("/{id}")
  public UserDto.Response get(@PathVariable Long id) { return service.get(id); }

  @PostMapping
  public ResponseEntity<UserDto.Response> create(@Valid @RequestBody UserDto.CreateRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @PutMapping("/{id}")
  public UserDto.Response update(@PathVariable Long id, @Valid @RequestBody UserDto.UpdateRequest r) { return service.update(id, r); }

  @PatchMapping("/{id}/status")
  public UserDto.Response status(@PathVariable Long id, @Valid @RequestBody UserDto.StatusRequest r, @AuthenticationPrincipal AuthenticatedUser actor) {
    return service.setStatus(id, r.status(), actor);
  }
}
