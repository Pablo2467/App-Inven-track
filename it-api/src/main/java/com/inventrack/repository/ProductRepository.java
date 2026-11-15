package com.inventrack.repository;

import com.inventrack.entity.Product;
import com.inventrack.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  boolean existsByCodeIgnoreCase(String code);
  boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
  long countByStatus(Status status);
}
