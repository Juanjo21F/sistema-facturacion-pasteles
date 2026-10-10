package com.pasteleria.facturacion.presentation.handler;

import com.pasteleria.facturacion.domain.exception.AccesoDenegadoException;
import com.pasteleria.facturacion.domain.exception.AutenticacionException;
import com.pasteleria.facturacion.domain.exception.AuthcoreNoDisponibleException;
import com.pasteleria.facturacion.domain.exception.RecursoDuplicadoException;
import com.pasteleria.facturacion.domain.exception.RecursoNoEncontradoException;
import com.pasteleria.facturacion.domain.exception.ReglaDeNegocioException;
import com.pasteleria.facturacion.presentation.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/** Traduce excepciones a respuestas REST claras. Nunca expone stack traces. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> duplicado(RecursoDuplicadoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaDeNegocio(ReglaDeNegocioException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AutenticacionException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AutenticacionException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException ex) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AuthcoreNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> authcoreCaido(AuthcoreNoDisponibleException ex) {
        LOG.error("authcore-service no disponible", ex);
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autenticación no está disponible", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionBody(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> validacionParametros(ConstraintViolationException ex) {
        List<String> detalles = ex.getConstraintViolations().stream().map(v -> v.getMessage()).toList();
        return responder(HttpStatus.BAD_REQUEST, "Hay parámetros inválidos en la solicitud", detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> solicitudMalFormada(Exception ex) {
        return responder(HttpStatus.BAD_REQUEST, "La solicitud está mal formada o le faltan parámetros", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        LOG.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intente de nuevo más tarde", List.of());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado)
                .body(ErrorResponse.de(estado.value(), estado.getReasonPhrase(), mensaje, detalles));
    }
}
