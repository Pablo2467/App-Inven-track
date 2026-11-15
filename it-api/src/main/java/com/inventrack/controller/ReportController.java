package com.inventrack.controller;

import com.inventrack.dto.AlertDto;
import com.inventrack.dto.CommonDto.PageResponse;
import com.inventrack.dto.DashboardDto;
import com.inventrack.dto.InventoryDto;
import com.inventrack.dto.MovementDto;
import com.inventrack.entity.MovementType;
import com.inventrack.service.AlertService;
import com.inventrack.service.DashboardService;
import com.inventrack.service.InventoryService;
import com.inventrack.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

/** Consultas de solo lectura: inventario, alertas, movimientos y dashboard. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {
  private final InventoryService inventory;
  private final AlertService alerts;
  private final MovementService movements;
  private final DashboardService dashboard;

  @GetMapping("/inventory")
  public List<InventoryDto.Item> inventory(@RequestParam(required = false) String q, @RequestParam(required = false) Integer days) {
    return inventory.list(q, days);
  }

  @GetMapping("/expiration-alerts")
  public AlertDto.Response alerts(@RequestParam(required = false) Integer days, @RequestParam(required = false) String type) {
    return alerts.get(days, type);
  }

  @GetMapping("/movements")
  public PageResponse<MovementDto.Response> movements(@RequestParam(required = false) MovementType type, @RequestParam(required = false) Long productId,
                                                      @RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return movements.list(type, productId, userId, from, to, pageable);
  }

  @GetMapping("/dashboard")
  public DashboardDto.Response dashboard() { return dashboard.get(); }
}
