package com.chn.prestamos.repository;

import com.chn.prestamos.entity.EstadoSolicitud;
import com.chn.prestamos.entity.SolicitudPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudPrestamoRepository
    extends JpaRepository<SolicitudPrestamo, Long> {

    List<SolicitudPrestamo> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudPrestamo>
        findByCliente_IdOrderByFechaSolicitudDesc(Long clienteId);

    List<SolicitudPrestamo>
        findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);

    List<SolicitudPrestamo>
        findByCliente_IdAndEstadoOrderByFechaSolicitudDesc(
            Long clienteId,
            EstadoSolicitud estado
        );

    void deleteByCliente_Id(Long clienteId);
}