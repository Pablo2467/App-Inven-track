package com.inventrack.service;

import com.inventrack.dto.CommonDto.MessageResponse;
import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.SupplierDto;
import com.inventrack.entity.Status;
import com.inventrack.entity.Supplier;
import com.inventrack.exception.ConflictException;
import com.inventrack.exception.NotFoundException;
import com.inventrack.mapper.Mappers;
import com.inventrack.repository.LotRepository;
import com.inventrack.repository.StockEntryRepository;
import com.inventrack.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
  private final SupplierRepository suppliers;
  private final LotRepository lots;
  private final StockEntryRepository entries;

  @Transactional(readOnly = true)
  public PageResponse<SupplierDto.Response> list(String q, Status status, Pageable pageable) {
    Specification<Supplier> spec = (root, cq, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      if (q != null && !q.isBlank()) {
        String like = "%" + q.trim().toLowerCase() + "%";
        ps.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("taxId")), like),
            cb.like(cb.lower(cb.coalesce(root.<String>get("email"), "")), like)));
      }
      if (status != null) ps.add(cb.equal(root.get("status"), status));
      return cb.and(ps.toArray(new Predicate[0]));
    };
    return PageResponse.of(suppliers.findAll(spec, pageable), Mappers::supplier);
  }

  @Transactional(readOnly = true)
  public SupplierDto.Response get(Long id) { return Mappers.supplier(find(id)); }

  @Transactional
  public SupplierDto.Response create(SupplierDto.Request r) {
    String taxId = r.taxId().trim();
    if (suppliers.existsByTaxIdIgnoreCase(taxId)) throw new ConflictException("Ya existe un proveedor con el NIT/identificación " + taxId);
    Supplier s = new Supplier();
    apply(s, r, taxId);
    return Mappers.supplier(suppliers.save(s));
  }

  @Transactional
  public SupplierDto.Response update(Long id, SupplierDto.Request r) {
    Supplier s = find(id);
    String taxId = r.taxId().trim();
    if (suppliers.existsByTaxIdIgnoreCaseAndIdNot(taxId, id)) throw new ConflictException("Ya existe otro proveedor con el NIT/identificación " + taxId);
    apply(s, r, taxId);
    return Mappers.supplier(suppliers.save(s));
  }

  @Transactional
  public MessageResponse delete(Long id) {
    Supplier s = find(id);
    if (lots.existsBySupplierId(id) || entries.existsBySupplierId(id)) {
      s.setStatus(Status.INACTIVE);
      suppliers.save(s);
      return new MessageResponse("DEACTIVATED", "El proveedor tiene entradas registradas, por eso fue desactivado en lugar de eliminado.");
    }
    suppliers.delete(s);
    return new MessageResponse("DELETED", "Proveedor eliminado.");
  }

  private Supplier find(Long id) { return suppliers.findById(id).orElseThrow(() -> new NotFoundException("Proveedor no encontrado")); }

  private void apply(Supplier s, SupplierDto.Request r, String taxId) {
    s.setName(r.name().trim());
    s.setTaxId(taxId);
    s.setPhone(ProductService.blankToNull(r.phone()));
    s.setEmail(ProductService.blankToNull(r.email()));
    s.setAddress(ProductService.blankToNull(r.address()));
    s.setStatus(r.status() == null ? (s.getStatus() == null ? Status.ACTIVE : s.getStatus()) : r.status());
  }
}
