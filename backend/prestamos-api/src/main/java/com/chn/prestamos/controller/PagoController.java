package com.chn.prestamos.controller;

import com.chn.prestamos.dto.RegistrarPagoRequest;
import com.chn.prestamos.entity.Pago;
import com.chn.prestamos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "http://localhost:4200")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<Pago> listarTodos() {
        return pagoService.listarTodos();
    }

    @GetMapping("/prestamo/{prestamoId}")
    public List<Pago> listarPorPrestamo(
        @PathVariable Long prestamoId
    ) {
        return pagoService.listarPorPrestamo(prestamoId);
    }

    @PostMapping("/prestamo/{prestamoId}")
    public ResponseEntity<Pago> registrar(
        @PathVariable Long prestamoId,
        @Valid @RequestBody RegistrarPagoRequest request
    ) {
        Pago pagoRegistrado =
            pagoService.registrar(prestamoId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pagoRegistrado);
    }
}