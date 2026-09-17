package com.chn.prestamos.service;

import com.chn.prestamos.dto.CrearSolicitudRequest;
import com.chn.prestamos.dto.ResolverSolicitudRequest;
import com.chn.prestamos.entity.Cliente;
import com.chn.prestamos.entity.EstadoSolicitud;
import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.entity.SolicitudPrestamo;
import com.chn.prestamos.repository.PrestamoRepository;
import com.chn.prestamos.repository.SolicitudPrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SolicitudPrestamoService {

    private final SolicitudPrestamoRepository solicitudRepository;
    private final PrestamoRepository prestamoRepository;
    private final ClienteService clienteService;

    public SolicitudPrestamoService(
        SolicitudPrestamoRepository solicitudRepository,
        PrestamoRepository prestamoRepository,
        ClienteService clienteService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.prestamoRepository = prestamoRepository;
        this.clienteService = clienteService;
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    @Transactional(readOnly = true)
    public SolicitudPrestamo buscarPorId(Long id) {
        return solicitudRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Solicitud de préstamo no encontrada"
            ));
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarPorCliente(Long clienteId) {
        clienteService.buscarPorId(clienteId);

        return solicitudRepository
            .findByCliente_IdOrderByFechaSolicitudDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarPorEstado(
        EstadoSolicitud estado
    ) {
        return solicitudRepository
            .findByEstadoOrderByFechaSolicitudDesc(estado);
    }

    @Transactional(readOnly = true)
    public List<SolicitudPrestamo> listarPorClienteYEstado(
        Long clienteId,
        EstadoSolicitud estado
    ) {
        clienteService.buscarPorId(clienteId);

        return solicitudRepository
            .findByCliente_IdAndEstadoOrderByFechaSolicitudDesc(
                clienteId,
                estado
            );
    }

    public SolicitudPrestamo crear(CrearSolicitudRequest request) {
        Cliente cliente = clienteService.buscarPorId(request.clienteId());

        SolicitudPrestamo solicitud = new SolicitudPrestamo();
        solicitud.setCliente(cliente);
        solicitud.setMontoSolicitado(request.montoSolicitado());
        solicitud.setPlazoMeses(request.plazoMeses());
        solicitud.setDestinoPrestamo(request.destinoPrestamo());
        solicitud.setObservaciones(request.observaciones());
        solicitud.setEstado(EstadoSolicitud.EN_PROCESO);

        return solicitudRepository.save(solicitud);
    }

    public SolicitudPrestamo resolver(
        Long solicitudId,
        ResolverSolicitudRequest request
    ) {
        SolicitudPrestamo solicitud = buscarPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.EN_PROCESO) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La solicitud ya fue resuelta"
            );
        }

        if (request.estado() == EstadoSolicitud.EN_PROCESO) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La resolución debe ser APROBADA o RECHAZADA"
            );
        }

        if (
            request.estado() == EstadoSolicitud.APROBADA
                && request.tasaInteresAnual() == null
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La tasa de interés es obligatoria al aprobar"
            );
        }

        solicitud.setEstado(request.estado());
        solicitud.setComentarioResolucion(request.comentario());
        solicitud.setFechaResolucion(LocalDateTime.now());

        SolicitudPrestamo solicitudGuardada =
            solicitudRepository.save(solicitud);

        if (request.estado() == EstadoSolicitud.APROBADA) {
            Prestamo prestamo = new Prestamo();
            prestamo.setSolicitud(solicitudGuardada);
            prestamo.setMontoAprobado(
                solicitudGuardada.getMontoSolicitado()
            );
            prestamo.setTasaInteresAnual(
                request.tasaInteresAnual()
            );
            prestamo.setPlazoMeses(
                solicitudGuardada.getPlazoMeses()
            );
            prestamo.setMontoPagado(BigDecimal.ZERO);

            prestamoRepository.save(prestamo);
        }

        return solicitudGuardada;
    }
}