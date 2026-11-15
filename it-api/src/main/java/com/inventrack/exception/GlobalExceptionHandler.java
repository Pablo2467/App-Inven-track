package com.inventrack.exception;

import com.inventrack.dto.CommonDto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ErrorResponse> build(HttpStatus s, String msg, HttpServletRequest r, Map<String, String> details) {
    return ResponseEntity.status(s).body(new ErrorResponse(LocalDateTime.now(), s.value(), s.getReasonPhrase(), msg, r.getRequestURI(), details));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> api(ApiException e, HttpServletRequest r) { return build(e.getStatus(), e.getMessage(), r, null); }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> d = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors().forEach(f -> d.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return build(HttpStatus.BAD_REQUEST, "Hay campos inválidos en la solicitud", r, d);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorResponse> constraint(ConstraintViolationException e, HttpServletRequest r) { return build(HttpStatus.BAD_REQUEST, e.getMessage(), r, null); }

  @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, PropertyReferenceException.class})
  ResponseEntity<ErrorResponse> badInput(Exception e, HttpServletRequest r) {
    return build(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato o valor inválido (revisa fechas, números, estados y parámetros)", r, null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException e, HttpServletRequest r) {
    log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
    return build(HttpStatus.CONFLICT, "La operación viola una restricción de datos (registro duplicado o con relaciones existentes)", r, null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest r) { return build(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido para este recurso", r, null); }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorResponse> noResource(NoResourceFoundException e, HttpServletRequest r) { return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", r, null); }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> generic(Exception e, HttpServletRequest r) {
    log.error("Error no controlado en {}", r.getRequestURI(), e);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intenta de nuevo o contacta al administrador.", r, null);
  }
}
