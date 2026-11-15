package com.inventrack.dto;

import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CommonDto {
  private CommonDto() {}

  public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T, E> PageResponse<T> of(Page<E> p, Function<E, T> f) {
      return new PageResponse<>(p.getContent().stream().map(f).toList(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
  }

  public record MessageResponse(String result, String message) {}

  public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> details) {}
}
