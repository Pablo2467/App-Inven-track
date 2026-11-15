package com.inventrack.repository;

import com.inventrack.entity.Lot;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long>, JpaSpecificationExecutor<Lot> {
  boolean existsByProductId(Long productId);
  boolean existsBySupplierId(Long supplierId);
  boolean existsByProductIdAndLotNumberIgnoreCase(Long productId, String lotNumber);
  boolean existsByProductIdAndLotNumberIgnoreCaseAndIdNot(Long productId, String lotNumber, Long id);

  /** FIFO: lotes vigentes con existencias, del más antiguo al más reciente. Bloqueo de escritura para evitar descuentos simultáneos. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT l FROM Lot l WHERE l.product.id = :productId AND l.availableQuantity > 0 AND l.expirationDate >= :today ORDER BY l.entryDate ASC, l.id ASC")
  List<Lot> lockUsableLots(@Param("productId") Long productId, @Param("today") LocalDate today);

  /** Lotes vencidos con existencias (para dar de baja por vencimiento), también en orden FIFO. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT l FROM Lot l WHERE l.product.id = :productId AND l.availableQuantity > 0 AND l.expirationDate < :today ORDER BY l.entryDate ASC, l.id ASC")
  List<Lot> lockExpiredLots(@Param("productId") Long productId, @Param("today") LocalDate today);

  @Query("SELECT l FROM Lot l JOIN FETCH l.product JOIN FETCH l.supplier WHERE l.availableQuantity > 0")
  List<Lot> findAllWithStock();

  @Query("SELECT l FROM Lot l JOIN FETCH l.product JOIN FETCH l.supplier WHERE l.availableQuantity > 0 AND l.expirationDate BETWEEN :from AND :to ORDER BY l.expirationDate ASC")
  List<Lot> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query("SELECT l FROM Lot l JOIN FETCH l.product JOIN FETCH l.supplier WHERE l.availableQuantity > 0 AND l.expirationDate < :today ORDER BY l.expirationDate ASC")
  List<Lot> findExpired(@Param("today") LocalDate today);

  @Query("SELECT l FROM Lot l JOIN FETCH l.product JOIN FETCH l.supplier WHERE l.availableQuantity <= 0 ORDER BY l.updatedAt DESC")
  List<Lot> findDepleted(Pageable pageable);

  long countByAvailableQuantityLessThanEqual(BigDecimal zero);

  @Query("SELECT COALESCE(SUM(l.availableQuantity), 0) FROM Lot l WHERE l.availableQuantity > 0 AND l.expirationDate >= :today")
  BigDecimal sumUsableStock(@Param("today") LocalDate today);

  @Modifying
  @Query("UPDATE Lot l SET l.status = com.inventrack.entity.LotStatus.EXPIRED, l.updatedAt = :now WHERE l.status = com.inventrack.entity.LotStatus.AVAILABLE AND l.availableQuantity > 0 AND l.expirationDate < :today")
  int markExpired(@Param("today") LocalDate today, @Param("now") LocalDateTime now);
}
