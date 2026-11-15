package com.inventrack.controller;

import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.EntryDto;
import com.inventrack.dto.LotDto;
import com.inventrack.entity.LotStatus;
import com.inventrack.security.AuthenticatedUser;
import com.inventrack.service.LotService;
import com.inventrack.service.StockEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class LotController {
  private final LotService service;
  private final StockEntryService entries;

  @GetMapping
  public PageResponse<LotDto.Response> list(@RequestParam(required = false) Long productId, @RequestParam(required = false) Long supplierId,
                                            @RequestParam(required = false) LotStatus status, @RequestParam(required = false) String q,
                                            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
    return service.list(productId, supplierId, status, q, pageable);
  }

  @GetMapping("/{id}")
  public LotDto.Response get(@PathVariable Long id) { return service.get(id); }

  /** Registrar un lote equivale a registrar una entrada: crea lote, entrada y movimiento en una sola transacción. */
  @PostMapping
  public ResponseEntity<LotDto.Response> create(@Valid @RequestBody EntryDto.Request r, @AuthenticationPrincipal AuthenticatedUser user) {
    EntryDto.Response entry = entries.register(r, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(service.get(entry.lotId()));
  }

  @PutMapping("/{id}")
  public LotDto.Response update(@PathVariable Long id, @Valid @RequestBody LotDto.UpdateRequest r) { return service.update(id, r); }
}
