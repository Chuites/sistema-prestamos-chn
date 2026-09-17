package com.chn.prestamos.service;

import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.repository.PrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final ClienteService clienteService;

    public PrestamoService(
        PrestamoRepository prestamoRepository,
        ClienteService clienteService
    ) {
        this.prestamoRepository = prestamoRepository;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarTodos() {
        return prestamoRepository
            .findAllByOrderByFechaAprobacionDesc();
    }

    @Transactional(readOnly = true)
    public Prestamo buscarPorId(Long id) {
        return prestamoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Préstamo no encontrado"
            ));
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarPorCliente(Long clienteId) {
        clienteService.buscarPorId(clienteId);

        return prestamoRepository
            .findBySolicitud_Cliente_IdOrderByFechaAprobacionDesc(
                clienteId
            );
    }

    @Transactional(readOnly = true)
    public Prestamo buscarPorSolicitud(Long solicitudId) {
        return prestamoRepository.findBySolicitud_Id(solicitudId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe un préstamo para esa solicitud"
            ));
    }
}