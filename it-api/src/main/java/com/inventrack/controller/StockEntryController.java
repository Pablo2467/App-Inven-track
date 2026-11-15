package com.inventrack.controller;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.EntryDto;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.service.StockEntryService;
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
@RequestMapping("/api/v1/stock-entries")
@RequiredArgsConstructor
public class StockEntryController {
  private final StockEntryService service;

  @GetMapping
  public PageResponse<EntryDto.Response> list(@RequestParam(required = false) Long productId, @RequestParam(required = false) Long supplierId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                              @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return service.list(productId, supplierId, from, to, pageable);
  }

  @GetMapping("/{id}")
  public EntryDto.Response get(@PathVariable Long id) { return service.get(id); }

  @PostMapping
  public ResponseEntity<EntryDto.Response> register(@Valid @RequestBody EntryDto.Request r, @AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r, user));
  }
}
