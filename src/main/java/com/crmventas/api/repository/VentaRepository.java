package com.crmventas.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Venta;

import java.util.Optional;
import java.util.UUID;

public interface VentaRepository extends JpaRepository<Venta, UUID> {

    boolean existsByCodigoVenta(String codigoVenta);

    Optional<Venta> findByIdAndEliminadoFalse(UUID id);

    @Query("""
        SELECT v FROM Venta v
        JOIN FETCH v.campana c
        JOIN FETCH v.agente a
        JOIN FETCH v.estado e
        LEFT JOIN FETCH v.cliente cl
        WHERE v.eliminado = false
          AND (:campanaId IS NULL OR c.id = :campanaId)
          AND (:agenteId IS NULL OR a.id = :agenteId)
          AND (:estadoCodigo IS NULL OR e.codigo = :estadoCodigo)
          AND (:tieneAlerta IS NULL OR v.tieneAlerta = :tieneAlerta)
        """)
    Page<Venta> filtrar(
        @Param("campanaId")    UUID campanaId,
        @Param("agenteId")     UUID agenteId,
        @Param("estadoCodigo") String estadoCodigo,
        @Param("tieneAlerta")  Boolean tieneAlerta,
        Pageable pageable
    );

    @Query("""
        SELECT v FROM Venta v
        WHERE v.cliente.id = :clienteId AND v.eliminado = false
        ORDER BY v.fechaVenta DESC
        """)
    Page<Venta> findByClienteId(@Param("clienteId") UUID clienteId, Pageable pageable);
}
