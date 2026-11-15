package com.inventrack.repository;

import com.inventrack.entity.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface StockEntryRepository extends JpaRepository<StockEntry, Long>, JpaSpecificationExecutor<StockEntry> {
  boolean existsBySupplierId(Long supplierId);

  @Query("SELECT e.entryDate, SUM(e.quantity) FROM StockEntry e WHERE e.entryDate >= :from GROUP BY e.entryDate")
  List<Object[]> dailyTotals(@Param("from") LocalDate from);
}
