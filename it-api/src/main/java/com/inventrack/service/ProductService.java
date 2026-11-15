package com.inventrack.service;

import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.ProductDto;
import com.inventrack.entity.Product;
import com.inventrack.entity.Status;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.InventoryMovementRepository;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository products;
  private final LotRepository lots;
  private final InventoryMovementRepository movements;

  @Transactional(readOnly = true)
  public PageResponse<ProductDto.Response> list(String q, Status status, Pageable pageable) {
    Specification<Product> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        ps.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("code")), like),
            cb.like(cb.lower(cb.coalesce(root.<String>get("category"), "")), like)));
      }
      if (status != null) ps.add(cb.equal(root.get("status"), status));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(products.findAll(spec, pageable), Mappers::product);
  }

  @Transactional(readOnly = true)
  public ProductDto.Response get(Long id) { return Mappers.product(find(id)); }

  @Transactional
  public ProductDto.Response create(ProductDto.Request r) {
    String code = r.code().trim().toUpperCase();
    if (products.existsByCodeIgnoreCase(code)) throw new ConflictException("Ya existe un producto con el código " + code);
    Product p = new Product();
    apply(p, r, code);
    return Mappers.product(products.save(p));
  }

  @Transactional
  public ProductDto.Response update(Long id, ProductDto.Request r) {
    Product p = find(id);
    String code = r.code().trim().toUpperCase();
    if (products.existsByCodeIgnoreCaseAndIdNot(code, id)) throw new ConflictException("Ya existe otro producto con el código " + code);
    apply(p, r, code);
    return Mappers.product(products.save(p));
  }

  /** Si el producto tiene lotes o movimientos se desactiva (se conserva el historial); si no, se elimina. */
  @Transactional
  public MessageResponse delete(Long id) {
    Product p = find(id);
    if (lots.existsByProductId(id) || movements.existsByProductId(id)) {
      p.setStatus(Status.INACTIVE);
      products.save(p);
      return new MessageResponse("DEACTIVATED", "El producto tiene historial de inventario, por eso fue desactivado en lugar de eliminado.");
    }
    products.delete(p);
    return new MessageResponse("DELETED", "Producto eliminado.");
  }

  private Product find(Long id) { return products.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado")); }

  private void apply(Product p, ProductDto.Request r, String code) {
    p.setCode(code);
    p.setName(r.name().trim());
    p.setDescription(blankToNull(r.description()));
    p.setCategory(blankToNull(r.category()));
    p.setUnit(r.unit().trim());
    p.setMinStock(r.minStock() == null ? BigDecimal.ZERO : r.minStock());
    p.setStatus(r.status() == null ? (p.getStatus() == null ? Status.ACTIVE : p.getStatus()) : r.status());
  }

  static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
