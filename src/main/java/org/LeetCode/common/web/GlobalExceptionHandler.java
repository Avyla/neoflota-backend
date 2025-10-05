package org.LeetCode.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.LeetCode.common.exceptions.BadRequestException;
import org.LeetCode.common.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ProblemDetail base(HttpStatus status, String title, String detail, String path) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("path", path);
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return base(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return base(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toMap)
                .toList();
        var pd = base(HttpStatus.BAD_REQUEST, "Error de validación", "Datos inválidos", req.getRequestURI());
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var pd = base(HttpStatus.BAD_REQUEST, "Parámetros inválidos", ex.getMessage(), req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return base(HttpStatus.BAD_REQUEST, "JSON mal formado", ex.getMostSpecificCause().getMessage(), req.getRequestURI());
    }

    private Map<String, String> toMap(FieldError fe) {
        return Map.of("field", fe.getField(), "message", String.valueOf(fe.getDefaultMessage()));
    }
}
