package com.chn.prestamos.repository;

import com.chn.prestamos.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findAllByOrderByFechaAprobacionDesc();

    List<Prestamo> findBySolicitud_Cliente_IdOrderByFechaAprobacionDesc(
        Long clienteId
    );

    Optional<Prestamo> findBySolicitud_Id(Long solicitudId);

    boolean existsBySolicitud_Id(Long solicitudId);

    void deleteBySolicitud_Cliente_Id(Long clienteId);
}