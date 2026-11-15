package com.inventrack.controller;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.ExitDto;
import com.inventrack.entity.ExitReason;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.service.StockExitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/stock-exits")
@RequiredArgsConstructor
public class StockExitController {
  private final StockExitService service;

  @GetMapping
  public PageResponse<ExitDto.Response> list(@RequestParam(required = false) Long productId, @RequestParam(required = false) ExitReason reason,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                             @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return service.list(productId, reason, from, to, pageable);
  }

  @GetMapping("/{id}")
  public ExitDto.Response get(@PathVariable Long id) { return service.get(id); }

  @PostMapping
  public ResponseEntity<ExitDto.Response> register(@Valid @RequestBody ExitDto.Request r, @AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r, user));
  }
}
