package com.inventrack.repository;

import com.inventrack.entity.StockExit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface StockExitRepository extends JpaRepository<StockExit, Long>, JpaSpecificationExecutor<StockExit> {
  @Query("SELECT e.exitDate, SUM(e.quantity) FROM StockExit e WHERE e.exitDate >= :from GROUP BY e.exitDate")
  List<Object[]> dailyTotals(@Param("from") LocalDate from);
}
