package com.chn.prestamos.service;

import com.chn.prestamos.dto.RegistrarPagoRequest;
import com.chn.prestamos.entity.EstadoPrestamo;
import com.chn.prestamos.entity.Pago;
import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.repository.PagoRepository;
import com.chn.prestamos.repository.PrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PrestamoRepository prestamoRepository;
    private final PrestamoService prestamoService;

    public PagoService(
        PagoRepository pagoRepository,
        PrestamoRepository prestamoRepository,
        PrestamoService prestamoService
    ) {
        this.pagoRepository = pagoRepository;
        this.prestamoRepository = prestamoRepository;
        this.prestamoService = prestamoService;
    }

    @Transactional(readOnly = true)
    public List<Pago> listarTodos() {
        return pagoRepository.findAllByOrderByFechaPagoDesc();
    }

    @Transactional(readOnly = true)
    public List<Pago> listarPorPrestamo(Long prestamoId) {
        prestamoService.buscarPorId(prestamoId);

        return pagoRepository
            .findByPrestamo_IdOrderByFechaPagoDesc(prestamoId);
    }

    public Pago registrar(
        Long prestamoId,
        RegistrarPagoRequest request
    ) {
        Prestamo prestamo = prestamoService.buscarPorId(prestamoId);

        if (prestamo.getEstado() == EstadoPrestamo.PAGADO) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El préstamo ya está pagado"
            );
        }

        if (
            request.monto().compareTo(
                prestamo.getSaldoPendiente()
            ) > 0
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El pago no puede superar el saldo pendiente"
            );
        }

        BigDecimal nuevoMontoPagado =
            prestamo.getMontoPagado().add(request.monto());

        BigDecimal nuevoSaldo =
            prestamo.getSaldoPendiente().subtract(request.monto());

        prestamo.setMontoPagado(nuevoMontoPagado);
        prestamo.setSaldoPendiente(nuevoSaldo);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            prestamo.setEstado(EstadoPrestamo.PAGADO);
        } else {
            prestamo.setEstado(EstadoPrestamo.PARCIAL);
        }

        prestamoRepository.save(prestamo);

        Pago pago = new Pago();
        pago.setPrestamo(prestamo);
        pago.setMonto(request.monto());
        pago.setMetodoPago("EFECTIVO");
        pago.setNumeroRecibo(request.numeroRecibo());
        pago.setObservaciones(request.observaciones());

        return pagoRepository.save(pago);
    }
}