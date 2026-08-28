package com.logitrack.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> manejarReglaDeNegocio(
            BusinessException exception,
            HttpServletRequest request) {

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarErroresDeValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String mensaje = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatearErrorDeCampo)
                .collect(Collectors.joining("; "));

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarViolacionesDeRestricciones(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        String mensaje = exception.getConstraintViolations()
                .stream()
                .map(violacion -> violacion.getPropertyPath()
                        + ": "
                        + violacion.getMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.BAD_REQUEST,
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarErrorDeAutenticacion(
            AuthenticationException exception,
            HttpServletRequest request) {

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas o usuario no autenticado",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(
            AccessDeniedException exception,
            HttpServletRequest request) {

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operación",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception exception,
            HttpServletRequest request) {

        ErrorResponse respuesta = construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta);
    }

    private ErrorResponse construirRespuesta(
            HttpStatus estado,
            String mensaje,
            String ruta) {

        return new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                ruta
        );
    }

    private String formatearErrorDeCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}