package com.chn.prestamos.config;

import com.chn.prestamos.dto.CrearSolicitudRequest;
import com.chn.prestamos.dto.RegistrarPagoRequest;
import com.chn.prestamos.dto.ResolverSolicitudRequest;
import com.chn.prestamos.entity.Cliente;
import com.chn.prestamos.entity.EstadoSolicitud;
import com.chn.prestamos.entity.Prestamo;
import com.chn.prestamos.entity.SolicitudPrestamo;
import com.chn.prestamos.repository.ClienteRepository;
import com.chn.prestamos.service.ClienteService;
import com.chn.prestamos.service.PagoService;
import com.chn.prestamos.service.PrestamoService;
import com.chn.prestamos.service.SolicitudPrestamoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log =
        LoggerFactory.getLogger(DataSeeder.class);

    private final ClienteService clienteService;
    private final SolicitudPrestamoService solicitudService;
    private final PrestamoService prestamoService;
    private final PagoService pagoService;
    private final ClienteRepository clienteRepository;

    public DataSeeder(
        ClienteService clienteService,
        SolicitudPrestamoService solicitudService,
        PrestamoService prestamoService,
        PagoService pagoService,
        ClienteRepository clienteRepository
    ) {
        this.clienteService = clienteService;
        this.solicitudService = solicitudService;
        this.prestamoService = prestamoService;
        this.pagoService = pagoService;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (clienteRepository.count() > 0) {
            log.info(
                "Datos demo omitidos: la base ya contiene registros"
            );
            return;
        }

        List<Cliente> clientes = crearClientes();
        int solicitudesCreadas = 0;

        for (SolicitudSeed seed : solicitudesSeed()) {
            crearSolicitud(clientes.get(seed.clienteIndex()), seed);
            solicitudesCreadas++;
        }

        log.info(
            "Datos demo cargados: {} clientes, {} solicitudes",
            clientes.size(),
            solicitudesCreadas
        );
    }

    private void crearSolicitud(Cliente cliente, SolicitudSeed seed) {
        SolicitudPrestamo solicitud = solicitudService.crear(
            new CrearSolicitudRequest(
                cliente.getId(),
                seed.monto(),
                seed.plazoMeses(),
                seed.destino(),
                seed.observaciones()
            )
        );

        if (seed.estado() == EstadoSolicitud.EN_PROCESO) {
            return;
        }

        solicitudService.resolver(
            solicitud.getId(),
            new ResolverSolicitudRequest(
                seed.estado(),
                seed.comentario(),
                seed.tasaInteresAnual()
            )
        );

        if (seed.estado() != EstadoSolicitud.APROBADA) {
            return;
        }

        Prestamo prestamo = prestamoService.buscarPorSolicitud(
            solicitud.getId()
        );

        for (PagoSeed pago : seed.pagos()) {
            pagoService.registrar(
                prestamo.getId(),
                new RegistrarPagoRequest(
                    pago.monto(),
                    pago.numeroRecibo(),
                    null
                )
            );
        }
    }

    private List<Cliente> crearClientes() {
        List<Cliente> clientes = new ArrayList<>();

        for (ClienteSeed seed : clientesSeed()) {
            Cliente cliente = new Cliente();
            cliente.setNombre(seed.nombre());
            cliente.setApellido(seed.apellido());
            cliente.setNumeroIdentificacion(seed.identificacion());
            cliente.setFechaNacimiento(seed.fechaNacimiento());
            cliente.setDireccion(seed.direccion());
            cliente.setCorreoElectronico(seed.correo());
            cliente.setTelefono(seed.telefono());
            clientes.add(clienteService.crear(cliente));
        }

        return clientes;
    }

    private List<ClienteSeed> clientesSeed() {
        return List.of(
            new ClienteSeed(
                "Ana Lucía", "López García", "1001",
                LocalDate.of(1990, 3, 14),
                "5a Avenida 12-34 Zona 1",
                "ana.lopez@example.com", "5555-1001"
            ),
            new ClienteSeed(
                "Carlos Alberto", "Méndez Ruiz", "1002",
                LocalDate.of(1985, 7, 22),
                "3a Calle 8-90 Zona 4",
                "carlos.mendez@example.com", "5555-1002"
            ),
            new ClienteSeed(
                "María José", "Ramírez Soto", "1003",
                LocalDate.of(1992, 11, 5),
                "7a Avenida 15-12 Zona 10",
                "maria.ramirez@example.com", "5555-1003"
            ),
            new ClienteSeed(
                "José Daniel", "Castillo Pérez", "1004",
                LocalDate.of(1978, 2, 18),
                "12 Calle 3-45 Zona 2",
                "jose.castillo@example.com", "5555-1004"
            ),
            new ClienteSeed(
                "Luisa Fernanda", "Herrera Molina", "1005",
                LocalDate.of(1995, 9, 30),
                "1a Calle 20-08 Zona 14",
                "luisa.herrera@example.com", "5555-1005"
            ),
            new ClienteSeed(
                "Pedro Antonio", "Vásquez Lima", "1006",
                LocalDate.of(1988, 12, 1),
                "9a Avenida 6-77 Zona 5",
                "pedro.vasquez@example.com", "5555-1006"
            ),
            new ClienteSeed(
                "Claudia Beatriz", "Rojas Aguilar", "1007",
                LocalDate.of(1983, 4, 25),
                "4a Calle 11-23 Zona 3",
                "claudia.rojas@example.com", "5555-1007"
            ),
            new ClienteSeed(
                "Juan Pablo", "Morales Estrada", "1008",
                LocalDate.of(1975, 6, 9),
                "6a Avenida 18-56 Zona 7",
                "juan.morales@example.com", "5555-1008"
            ),
            new ClienteSeed(
                "Sofía Alejandra", "Cruz Barrios", "1009",
                LocalDate.of(1998, 1, 27),
                "2a Calle 9-14 Zona 11",
                "sofia.cruz@example.com", "5555-1009"
            ),
            new ClienteSeed(
                "Ricardo Estuardo", "Díaz Fuentes", "1010",
                LocalDate.of(1980, 10, 16),
                "8a Avenida 22-31 Zona 9",
                "ricardo.diaz@example.com", "5555-1010"
            ),
            new ClienteSeed(
                "Elena Margarita", "Ortiz Villagrán", "1011",
                LocalDate.of(1993, 5, 3),
                "10 Calle 5-60 Zona 6",
                "elena.ortiz@example.com", "5555-1011"
            ),
            new ClienteSeed(
                "Fernando Andrés", "Girón Cifuentes", "1012",
                LocalDate.of(1987, 8, 21),
                "11 Avenida 14-49 Zona 13",
                "fernando.giron@example.com", "5555-1012"
            )
        );
    }

    private List<SolicitudSeed> solicitudesSeed() {
        String aprobado = "Documentación verificada y capacidad de pago "
            + "aprobada.";
        String rechazado = "No cumple con la capacidad de pago requerida.";

        return List.of(
            new SolicitudSeed(
                0, new BigDecimal("15000.00"), 12, "Compra de vehículo",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("14.50"),
                List.of(new PagoSeed(new BigDecimal("2000.00"), "REC-0001"))
            ),
            new SolicitudSeed(
                0, new BigDecimal("12000.00"), 18,
                "Capital de trabajo para negocio", null,
                EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                1, new BigDecimal("8000.00"), 6, "Remodelación de vivienda",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("16.00"),
                List.of(
                    new PagoSeed(new BigDecimal("4000.00"), "REC-0002"),
                    new PagoSeed(new BigDecimal("4000.00"), "REC-0003")
                )
            ),
            new SolicitudSeed(
                1, new BigDecimal("5000.00"), 10, "Gastos médicos", null,
                EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            ),
            new SolicitudSeed(
                2, new BigDecimal("25000.00"), 24, "Compra de vehículo",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("11.75"), List.of()
            ),
            new SolicitudSeed(
                2, new BigDecimal("10000.00"), 12, "Estudios universitarios",
                null, EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                3, new BigDecimal("5000.00"), 10,
                "Compra de equipo de cómputo", null,
                EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            ),
            new SolicitudSeed(
                3, new BigDecimal("20000.00"), 36, "Compra de vivienda",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("10.25"),
                List.of(new PagoSeed(new BigDecimal("5000.00"), "REC-0004"))
            ),
            new SolicitudSeed(
                4, new BigDecimal("30000.00"), 48,
                "Capital de trabajo para negocio", null,
                EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("12.00"), List.of()
            ),
            new SolicitudSeed(
                4, new BigDecimal("6500.00"), 12,
                "Compra de electrodomésticos", null,
                EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                5, new BigDecimal("7000.00"), 12, "Gastos médicos", null,
                EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("15.50"),
                List.of(new PagoSeed(new BigDecimal("7000.00"), "REC-0005"))
            ),
            new SolicitudSeed(
                5, new BigDecimal("4500.00"), 6,
                "Compra de electrodomésticos", null,
                EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            ),
            new SolicitudSeed(
                6, new BigDecimal("18000.00"), 24, "Estudios universitarios",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("13.25"),
                List.of(
                    new PagoSeed(new BigDecimal("3000.00"), "REC-0006"),
                    new PagoSeed(new BigDecimal("2000.00"), "REC-0007")
                )
            ),
            new SolicitudSeed(
                6, new BigDecimal("9000.00"), 12, "Compra de vehículo",
                null, EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                7, new BigDecimal("50000.00"), 60, "Compra de vivienda",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("9.50"), List.of()
            ),
            new SolicitudSeed(
                7, new BigDecimal("6000.00"), 12, "Remodelación de vivienda",
                null, EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            ),
            new SolicitudSeed(
                8, new BigDecimal("11000.00"), 18,
                "Capital de trabajo para negocio", null,
                EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("14.00"),
                List.of(new PagoSeed(new BigDecimal("1000.00"), "REC-0008"))
            ),
            new SolicitudSeed(
                8, new BigDecimal("3500.00"), 6,
                "Compra de electrodomésticos", null,
                EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                9, new BigDecimal("22000.00"), 36, "Compra de vehículo",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("11.00"), List.of()
            ),
            new SolicitudSeed(
                9, new BigDecimal("8000.00"), 12, "Gastos médicos", null,
                EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            ),
            new SolicitudSeed(
                10, new BigDecimal("15000.00"), 24, "Estudios universitarios",
                null, EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("12.75"), List.of()
            ),
            new SolicitudSeed(
                10, new BigDecimal("5000.00"), 10,
                "Compra de equipo de cómputo", null,
                EstadoSolicitud.EN_PROCESO, null, null, List.of()
            ),
            new SolicitudSeed(
                11, new BigDecimal("40000.00"), 48,
                "Capital de trabajo para negocio", null,
                EstadoSolicitud.APROBADA, aprobado,
                new BigDecimal("10.75"),
                List.of(
                    new PagoSeed(new BigDecimal("10000.00"), "REC-0009"),
                    new PagoSeed(new BigDecimal("5000.00"), "REC-0010")
                )
            ),
            new SolicitudSeed(
                11, new BigDecimal("7500.00"), 12,
                "Remodelación de vivienda", null,
                EstadoSolicitud.RECHAZADA, rechazado, null, List.of()
            )
        );
    }

    private record ClienteSeed(
        String nombre,
        String apellido,
        String identificacion,
        LocalDate fechaNacimiento,
        String direccion,
        String correo,
        String telefono
    ) {
    }

    private record PagoSeed(BigDecimal monto, String numeroRecibo) {
    }

    private record SolicitudSeed(
        int clienteIndex,
        BigDecimal monto,
        int plazoMeses,
        String destino,
        String observaciones,
        EstadoSolicitud estado,
        String comentario,
        BigDecimal tasaInteresAnual,
        List<PagoSeed> pagos
    ) {
    }
}
