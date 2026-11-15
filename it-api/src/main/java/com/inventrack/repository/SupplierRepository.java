package com.inventrack.repository;

import com.inventrack.entity.Status;
import com.inventrack.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
  boolean existsByTaxIdIgnoreCase(String taxId);
  boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);
  long countByStatus(Status status);
}
