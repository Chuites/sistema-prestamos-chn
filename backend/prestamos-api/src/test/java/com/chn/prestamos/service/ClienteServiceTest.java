package com.chn.prestamos.service;

import com.chn.prestamos.entity.Cliente;
import com.chn.prestamos.repository.ClienteRepository;
import com.chn.prestamos.repository.PagoRepository;
import com.chn.prestamos.repository.PrestamoRepository;
import com.chn.prestamos.repository.SolicitudPrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private SolicitudPrestamoRepository solicitudRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteValido() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setApellido("Gómez");
        cliente.setNumeroIdentificacion("1234567890101");
        cliente.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        cliente.setDireccion("5ta avenida 1-23 zona 1");
        cliente.setCorreoElectronico("ana@example.com");
        cliente.setTelefono("5555-5555");
        return cliente;
    }

    @Test
    void crearRechazaIdentificacionDuplicada() {
        Cliente cliente = clienteValido();
        when(clienteRepository.existsByNumeroIdentificacion(
            "1234567890101"
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> clienteService.crear(cliente)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearGuardaClienteSinId() {
        Cliente cliente = clienteValido();
        cliente.setId(99L);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        clienteService.crear(cliente);

        assertNull(cliente.getId());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void eliminarBorraPagosPrestamosYSolicitudesDelCliente() {
        Cliente cliente = clienteValido();
        cliente.setId(7L);
        when(clienteRepository.findById(7L))
            .thenReturn(Optional.of(cliente));

        clienteService.eliminar(7L);

        InOrder orden = inOrder(
            pagoRepository,
            prestamoRepository,
            solicitudRepository,
            clienteRepository
        );
        orden.verify(pagoRepository)
            .deleteByPrestamo_Solicitud_Cliente_Id(7L);
        orden.verify(prestamoRepository)
            .deleteBySolicitud_Cliente_Id(7L);
        orden.verify(solicitudRepository).deleteByCliente_Id(7L);
        orden.verify(clienteRepository).delete(cliente);
    }

    @Test
    void buscarPorIdLanza404SiNoExiste() {
        when(clienteRepository.findById(1L))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> clienteService.buscarPorId(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
