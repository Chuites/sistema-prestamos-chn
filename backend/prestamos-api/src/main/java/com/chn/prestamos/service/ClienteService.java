package com.chn.prestamos.service;

import com.chn.prestamos.entity.Cliente;
import com.chn.prestamos.repository.ClienteRepository;
import com.chn.prestamos.repository.PagoRepository;
import com.chn.prestamos.repository.PrestamoRepository;
import com.chn.prestamos.repository.SolicitudPrestamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;
    private final PrestamoRepository prestamoRepository;
    private final SolicitudPrestamoRepository solicitudRepository;

    public ClienteService(
        ClienteRepository clienteRepository,
        PagoRepository pagoRepository,
        PrestamoRepository prestamoRepository,
        SolicitudPrestamoRepository solicitudRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.pagoRepository = pagoRepository;
        this.prestamoRepository = prestamoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cliente no encontrado"
            ));
    }

    public Cliente crear(Cliente cliente) {
        if (clienteRepository.existsByNumeroIdentificacion(
            cliente.getNumeroIdentificacion()
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe un cliente con ese número de identificación"
            );
        }

        cliente.setId(null);
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datosCliente) {
        Cliente cliente = buscarPorId(id);

        if (clienteRepository.existsByNumeroIdentificacionAndIdNot(
            datosCliente.getNumeroIdentificacion(),
            id
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otro cliente con ese número de identificación"
            );
        }

        cliente.setNombre(datosCliente.getNombre());
        cliente.setApellido(datosCliente.getApellido());
        cliente.setNumeroIdentificacion(
            datosCliente.getNumeroIdentificacion()
        );
        cliente.setFechaNacimiento(
            datosCliente.getFechaNacimiento()
        );
        cliente.setDireccion(datosCliente.getDireccion());
        cliente.setCorreoElectronico(
            datosCliente.getCorreoElectronico()
        );
        cliente.setTelefono(datosCliente.getTelefono());

        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);

        pagoRepository.deleteByPrestamo_Cliente_Id(id);
        pagoRepository.flush();

        prestamoRepository.deleteByCliente_Id(id);
        prestamoRepository.flush();

        solicitudRepository.deleteByCliente_Id(id);
        solicitudRepository.flush();

        clienteRepository.delete(cliente);
    }
}