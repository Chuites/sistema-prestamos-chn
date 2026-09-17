package com.chn.prestamos.controller;

import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.service.PrestamoService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "http://localhost:4200")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listarTodos() {
        return prestamoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Prestamo buscarPorId(@PathVariable Long id) {
        return prestamoService.buscarPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Prestamo> listarPorCliente(
        @PathVariable Long clienteId
    ) {
        return prestamoService.listarPorCliente(clienteId);
    }

    @GetMapping("/solicitud/{solicitudId}")
    public Prestamo buscarPorSolicitud(
        @PathVariable Long solicitudId
    ) {
        return prestamoService.buscarPorSolicitud(solicitudId);
    }
}