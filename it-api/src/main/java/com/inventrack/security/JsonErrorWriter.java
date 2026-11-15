package com.inventrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrack.dto.CommonDto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

/** Escribe errores JSON para respuestas generadas dentro de la cadena de seguridad (401/403). */
@Component
public class JsonErrorWriter {
  private final ObjectMapper mapper;
  public JsonErrorWriter(ObjectMapper mapper) { this.mapper = mapper; }

  public void write(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String message) throws IOException {
    res.setStatus(status.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getWriter(), new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI(), null));
  }
}
