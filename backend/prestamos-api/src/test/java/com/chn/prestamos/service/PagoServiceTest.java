package com.chn.prestamos.service;

import com.chn.prestamos.dto.RegistrarPagoRequest;
import com.chn.prestamos.entity.EstadoPrestamo;
import com.chn.prestamos.entity.Pago;
import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.repository.PagoRepository;
import com.chn.prestamos.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private PrestamoService prestamoService;

    @InjectMocks
    private PagoService pagoService;

    private Prestamo prestamo(String montoAprobado, String montoPagado) {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(1L);
        prestamo.setMontoAprobado(new BigDecimal(montoAprobado));
        prestamo.setMontoPagado(new BigDecimal(montoPagado));
        return prestamo;
    }

    @Test
    void registrarPagoParcialActualizaSaldoYEstado() {
        Prestamo prestamo = prestamo("1000.00", "0.00");
        when(prestamoService.buscarPorId(1L)).thenReturn(prestamo);
        when(pagoRepository.save(any(Pago.class)))
            .thenAnswer((invocacion) -> invocacion.getArgument(0));

        pagoService.registrar(
            1L,
            new RegistrarPagoRequest(
                new BigDecimal("600.00"),
                "REC-1",
                null
            )
        );

        assertEquals(
            new BigDecimal("600.00"),
            prestamo.getMontoPagado()
        );
        assertEquals(
            new BigDecimal("400.00"),
            prestamo.getSaldoPendiente()
        );
        assertEquals(EstadoPrestamo.PARCIAL, prestamo.getEstado());
        verify(prestamoRepository).save(prestamo);
    }

    @Test
    void registrarPagoTotalMarcaPagado() {
        Prestamo prestamo = prestamo("1000.00", "400.00");
        when(prestamoService.buscarPorId(1L)).thenReturn(prestamo);
        when(pagoRepository.save(any(Pago.class)))
            .thenAnswer((invocacion) -> invocacion.getArgument(0));

        pagoService.registrar(
            1L,
            new RegistrarPagoRequest(
                new BigDecimal("600.00"),
                "REC-2",
                null
            )
        );

        assertEquals(
            0,
            prestamo.getSaldoPendiente().compareTo(BigDecimal.ZERO)
        );
        assertEquals(EstadoPrestamo.PAGADO, prestamo.getEstado());
    }

    @Test
    void registrarPagoMayorAlSaldoEsInvalido() {
        Prestamo prestamo = prestamo("1000.00", "0.00");
        when(prestamoService.buscarPorId(1L)).thenReturn(prestamo);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> pagoService.registrar(
                1L,
                new RegistrarPagoRequest(
                    new BigDecimal("1500.00"),
                    "REC-3",
                    null
                )
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void registrarPagoSobrePrestamoPagadoEsInvalido() {
        Prestamo prestamo = prestamo("1000.00", "1000.00");
        when(prestamoService.buscarPorId(1L)).thenReturn(prestamo);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> pagoService.registrar(
                1L,
                new RegistrarPagoRequest(
                    new BigDecimal("100.00"),
                    "REC-4",
                    null
                )
            )
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void registrarPagoConReciboDuplicadoEsConflicto() {
        Prestamo prestamo = prestamo("1000.00", "0.00");
        when(prestamoService.buscarPorId(1L)).thenReturn(prestamo);
        when(pagoRepository.existsByNumeroRecibo("REC-1"))
            .thenReturn(true);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> pagoService.registrar(
                1L,
                new RegistrarPagoRequest(
                    new BigDecimal("100.00"),
                    "REC-1",
                    null
                )
            )
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(pagoRepository, never()).save(any());
        verify(prestamoRepository, never()).save(any());
    }
}
