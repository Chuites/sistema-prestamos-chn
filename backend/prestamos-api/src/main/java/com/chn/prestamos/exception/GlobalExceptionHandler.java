package com.chn.prestamos.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresDeValidacion(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        return respuestaValidacion(errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarRestricciones(
        ConstraintViolationException ex
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach((violacion) ->
            errores.putIfAbsent(
                violacion.getPropertyPath().toString(),
                violacion.getMessage()
            )
        );

        return respuestaValidacion(errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarCuerpoIlegible(
        HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.badRequest().body(Map.of(
            "mensaje",
            "El cuerpo de la solicitud tiene un formato inválido. "
                + "Verifica que los números y las fechas estén bien escritos."
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarParametroInvalido(
        MethodArgumentTypeMismatchException ex
    ) {
        return ResponseEntity.badRequest().body(Map.of(
            "mensaje",
            "El valor enviado para '" + ex.getName() + "' no es válido."
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(
        DataIntegrityViolationException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "mensaje",
            "La operación viola una restricción de la base de datos."
        ));
    }

    private ResponseEntity<Map<String, Object>> respuestaValidacion(
        Map<String, String> errores
    ) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", "Hay datos inválidos en la solicitud.");
        cuerpo.put("errores", errores);

        return ResponseEntity.badRequest().body(cuerpo);
    }
}
