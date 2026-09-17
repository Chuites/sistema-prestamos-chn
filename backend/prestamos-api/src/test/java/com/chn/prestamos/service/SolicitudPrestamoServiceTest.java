package com.chn.prestamos.service;

import com.chn.prestamos.dto.CrearSolicitudRequest;
import com.chn.prestamos.dto.ResolverSolicitudRequest;
import com.chn.prestamos.entity.Cliente;
import com.chn.prestamos.entity.EstadoPrestamo;
import com.chn.prestamos.entity.EstadoSolicitud;
import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.entity.SolicitudPrestamo;
import com.chn.prestamos.repository.PrestamoRepository;
import com.chn.prestamos.repository.SolicitudPrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudPrestamoServiceTest {

    @Mock
    private SolicitudPrestamoRepository solicitudRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private SolicitudPrestamoService servicio;

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNombre("Luis");
        cliente.setApellido("Pérez");
        return cliente;
    }

    private SolicitudPrestamo solicitud(EstadoSolicitud estado) {
        SolicitudPrestamo solicitud = new SolicitudPrestamo();
        solicitud.setId(10L);
        solicitud.setCliente(cliente());
        solicitud.setMontoSolicitado(new BigDecimal("5000.00"));
        solicitud.setPlazoMeses(24);
        solicitud.setDestinoPrestamo("Compra de vehículo");
        solicitud.setEstado(estado);
        return solicitud;
    }

    @Test
    void crearRegistraSolicitudEnProceso() {
        when(clienteService.buscarPorId(3L)).thenReturn(cliente());
        when(solicitudRepository.save(any(SolicitudPrestamo.class)))
            .thenAnswer((invocacion) -> invocacion.getArgument(0));

        CrearSolicitudRequest request = new CrearSolicitudRequest(
            3L,
            new BigDecimal("5000.00"),
            24,
            "Compra de vehículo",
            "Sin observaciones"
        );

        SolicitudPrestamo resultado = servicio.crear(request);

        assertEquals(EstadoSolicitud.EN_PROCESO, resultado.getEstado());
        assertEquals(
            new BigDecimal("5000.00"),
            resultado.getMontoSolicitado()
        );
        assertEquals(24, resultado.getPlazoMeses());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void resolverSolicitudYaResueltaLanza409() {
        when(solicitudRepository.findById(10L))
            .thenReturn(Optional.of(solicitud(EstadoSolicitud.APROBADA)));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> servicio.resolver(
                10L,
                new ResolverSolicitudRequest(
                    EstadoSolicitud.APROBADA,
                    "ok",
                    new BigDecimal("12.50")
                )
            )
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void resolverConEstadoEnProcesoEsInvalido() {
        when(solicitudRepository.findById(10L))
            .thenReturn(Optional.of(solicitud(EstadoSolicitud.EN_PROCESO)));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> servicio.resolver(
                10L,
                new ResolverSolicitudRequest(
                    EstadoSolicitud.EN_PROCESO,
                    "ok",
                    null
                )
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void aprobarSinTasaEsInvalido() {
        when(solicitudRepository.findById(10L))
            .thenReturn(Optional.of(solicitud(EstadoSolicitud.EN_PROCESO)));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> servicio.resolver(
                10L,
                new ResolverSolicitudRequest(
                    EstadoSolicitud.APROBADA,
                    "ok",
                    null
                )
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void aprobarCreaPrestamoConSaldoInicial() {
        SolicitudPrestamo solicitud = solicitud(EstadoSolicitud.EN_PROCESO);
        when(solicitudRepository.findById(10L))
            .thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudPrestamo.class)))
            .thenAnswer((invocacion) -> invocacion.getArgument(0));

        servicio.resolver(
            10L,
            new ResolverSolicitudRequest(
                EstadoSolicitud.APROBADA,
                "Aprobado",
                new BigDecimal("12.50")
            )
        );

        ArgumentCaptor<Prestamo> captor =
            ArgumentCaptor.forClass(Prestamo.class);
        verify(prestamoRepository).save(captor.capture());

        Prestamo prestamo = captor.getValue();
        assertEquals(
            new BigDecimal("5000.00"),
            prestamo.getMontoAprobado()
        );
        assertEquals(BigDecimal.ZERO, prestamo.getMontoPagado());
        assertEquals(
            new BigDecimal("5000.00"),
            prestamo.getSaldoPendiente()
        );
        assertEquals(EstadoPrestamo.PENDIENTE, prestamo.getEstado());
        assertEquals(
            new BigDecimal("12.50"),
            prestamo.getTasaInteresAnual()
        );
        assertEquals(EstadoSolicitud.APROBADA, solicitud.getEstado());
    }

    @Test
    void rechazarNoCreaPrestamo() {
        when(solicitudRepository.findById(10L))
            .thenReturn(Optional.of(solicitud(EstadoSolicitud.EN_PROCESO)));
        when(solicitudRepository.save(any(SolicitudPrestamo.class)))
            .thenAnswer((invocacion) -> invocacion.getArgument(0));

        servicio.resolver(
            10L,
            new ResolverSolicitudRequest(
                EstadoSolicitud.RECHAZADA,
                "Motivo",
                null
            )
        );

        verify(prestamoRepository, never()).save(any());
    }
}
