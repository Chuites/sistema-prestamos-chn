package com.chn.prestamos.controller;

import com.chn.prestamos.dto.CrearSolicitudRequest;
import com.chn.prestamos.dto.ResolverSolicitudRequest;
import com.chn.prestamos.entity.EstadoSolicitud;
import com.chn.prestamos.entity.SolicitudPrestamo;
import com.chn.prestamos.service.SolicitudPrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitudPrestamoController {

    private final SolicitudPrestamoService solicitudService;

    public SolicitudPrestamoController(
        SolicitudPrestamoService solicitudService
    ) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public List<SolicitudPrestamo> listarTodas() {
        return solicitudService.listarTodas();
    }

    @GetMapping("/{id}")
    public SolicitudPrestamo buscarPorId(@PathVariable Long id) {
        return solicitudService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<SolicitudPrestamo> listarPorCliente(
        @PathVariable Long clienteId
    ) {
        return solicitudService.listarPorCliente(clienteId);
    }

    @GetMapping("/estado/{estado}")
    public List<SolicitudPrestamo> listarPorEstado(
        @PathVariable EstadoSolicitud estado
    ) {
        return solicitudService.listarPorEstado(estado);
    }

    @GetMapping("/cliente/{clienteId}/estado/{estado}")
    public List<SolicitudPrestamo> listarPorClienteYEstado(
        @PathVariable Long clienteId,
        @PathVariable EstadoSolicitud estado
    ) {
        return solicitudService.listarPorClienteYEstado(
            clienteId,
            estado
        );
    }

    @PostMapping
    public ResponseEntity<SolicitudPrestamo> crear(
        @Valid @RequestBody CrearSolicitudRequest request
    ) {
        SolicitudPrestamo solicitudCreada =
            solicitudService.crear(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(solicitudCreada);
    }

    @PutMapping("/{id}/resolver")
    public SolicitudPrestamo resolver(
        @PathVariable Long id,
        @Valid @RequestBody ResolverSolicitudRequest request
    ) {
        return solicitudService.resolver(id, request);
    }
}