package com.crmventas.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Venta;

import java.time.LocalDate;
import java.util.List;
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

    // ── Nuevas para el dashboard ───────────────────────────────────────────────
 
    /**
     * Cuenta ventas no finales del agente desde una fecha dada.
     */
    @Query("""
        SELECT COUNT(v) FROM Venta v
        WHERE v.agente.id = :agenteId
          AND v.eliminado = false
          AND v.estado.esFinal = false
          AND v.fechaVenta >= :desde
        """)
    int countVentasActivas(
        @Param("agenteId") UUID agenteId,
        @Param("desde")    LocalDate desde
    );
 
    /**
     * Suma monto total y comisión generada del agente en el período.
     * Retorna una proyección con getMontoTotal() y getComisionTotal().
     */
    @Query("""
        SELECT SUM(v.monto)            AS montoTotal,
               SUM(v.comisionGenerada) AS comisionTotal
        FROM Venta v
        WHERE v.agente.id = :agenteId
          AND v.eliminado = false
          AND v.estado.esFinal = false
          AND v.fechaVenta >= :desde
        """)
    MontoComisionProjection sumMontoYComision(
        @Param("agenteId") UUID agenteId,
        @Param("desde")    LocalDate desde
    );
 
    /**
     * Cuenta cuántas ventas del agente tienen alerta activa (sin filtro de período).
     */
    @Query("""
        SELECT COUNT(v) FROM Venta v
        WHERE v.agente.id  = :agenteId
          AND v.eliminado  = false
          AND v.tieneAlerta = true
        """)
    int countAlertas(@Param("agenteId") UUID agenteId);
 
    /**
     * Monto total por día en el período dado.
     */
    @Query("""
        SELECT v.fechaVenta AS fecha,
               SUM(v.monto) AS monto
        FROM Venta v
        WHERE v.agente.id = :agenteId
          AND v.eliminado = false
          AND v.fechaVenta >= :desde
        GROUP BY v.fechaVenta
        ORDER BY v.fechaVenta ASC
        """)
    List<TendenciaDiaProjection> tendenciaDiaria(
        @Param("agenteId") UUID agenteId,
        @Param("desde")    LocalDate desde
    );
 
    /**
     * Conteo de ventas agrupado por nombre de campaña.
     */
    @Query("""
        SELECT v.campana.nombre AS campana,
               COUNT(v)         AS total
        FROM Venta v
        WHERE v.agente.id = :agenteId
          AND v.eliminado = false
          AND v.fechaVenta >= :desde
        GROUP BY v.campana.nombre
        ORDER BY total DESC
        """)
    List<CampanaConteoProjection> ventasPorCampana(
        @Param("agenteId") UUID agenteId,
        @Param("desde")    LocalDate desde
    );
 
    /**
     * Conteo de ventas agrupado por estado.
     */
    @Query("""
        SELECT v.estado.nombre AS estado,
               v.estado.codigo AS codigo,
               COUNT(v)        AS total
        FROM Venta v
        WHERE v.agente.id = :agenteId
          AND v.eliminado = false
          AND v.fechaVenta >= :desde
        GROUP BY v.estado.nombre, v.estado.codigo
        ORDER BY total DESC
        """)
    List<EstadoConteoProjection> ventasPorEstado(
        @Param("agenteId") UUID agenteId,
        @Param("desde")    LocalDate desde
    );
 
    /**
     * Ventas observadas (con alerta activa) del agente.
     */
    @Query("""
        SELECT v FROM Venta v
        WHERE v.agente.id  = :agenteId
          AND v.eliminado  = false
          AND v.tieneAlerta = true
        ORDER BY v.actualizadoEn DESC
        """)
    List<Venta> alertasActivas(@Param("agenteId") UUID agenteId);
 
    // ── Proyecciones ──────────────────────────────────────────────────────────
 
    interface MontoComisionProjection {
        java.math.BigDecimal getMontoTotal();
        java.math.BigDecimal getComisionTotal();
    }
 
    interface TendenciaDiaProjection {
        java.time.LocalDate  getFecha();
        java.math.BigDecimal getMonto();
    }
 
    interface CampanaConteoProjection {
        String getCampana();
        long   getTotal();
    }
 
    interface EstadoConteoProjection {
        String getEstado();
        String getCodigo();
        long   getTotal();
    }
}
