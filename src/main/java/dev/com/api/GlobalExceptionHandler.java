package dev.com.api;

import dev.com.producto.DatoDuplicadoException;
import dev.com.producto.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(
            RecursoNoEncontradoException exception, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(
            DatoDuplicadoException exception, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> detalles = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage(),
                        (first, ignored) -> first,
                        TreeMap::new));
        return crearRespuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", request, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", request, Map.of());
    }

    private ResponseEntity<ApiError> crearRespuesta(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> detalles) {
        ApiError body = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
