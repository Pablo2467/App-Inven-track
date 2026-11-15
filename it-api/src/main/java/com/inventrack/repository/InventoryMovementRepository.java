package com.inventrack.repository;

import com.inventrack.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long>, JpaSpecificationExecutor<InventoryMovement> {
  boolean existsByProductId(Long productId);
  List<InventoryMovement> findByOutputIdOrderByIdAsc(Long outputId);
  List<InventoryMovement> findTop8ByOrderByIdDesc();
}
