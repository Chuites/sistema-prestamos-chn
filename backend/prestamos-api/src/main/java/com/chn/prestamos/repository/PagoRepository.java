package com.chn.prestamos.repository;

import com.chn.prestamos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findAllByOrderByFechaPagoDesc();

    List<Pago> findByPrestamo_IdOrderByFechaPagoDesc(
        Long prestamoId
    );

    boolean existsByNumeroRecibo(String numeroRecibo);

    void deleteByPrestamo_Solicitud_Cliente_Id(Long clienteId);
}