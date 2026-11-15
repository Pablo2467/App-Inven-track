package com.inventrack.controller;

import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.ProductDto;
import com.inventrack.entity.Status;
import com.inventrack.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService service;

  @GetMapping
  public PageResponse<ProductDto.Response> list(@RequestParam(required = false) String q, @RequestParam(required = false) Status status,
                                                @PageableDefault(size = 20, sort = "name") Pageable pageable) {
    return service.list(q, status, pageable);
  }

  @GetMapping("/{id}")
  public ProductDto.Response get(@PathVariable Long id) { return service.get(id); }

  @PostMapping
  public ResponseEntity<ProductDto.Response> create(@Valid @RequestBody ProductDto.Request r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @PutMapping("/{id}")
  public ProductDto.Response update(@PathVariable Long id, @Valid @RequestBody ProductDto.Request r) { return service.update(id, r); }

  @DeleteMapping("/{id}")
  public MessageResponse delete(@PathVariable Long id) { return service.delete(id); }
}
