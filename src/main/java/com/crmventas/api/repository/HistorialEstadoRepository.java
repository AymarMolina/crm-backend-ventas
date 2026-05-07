package com.crmventas.api.repository;

import com.crmventas.api.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
 
    /**
     * Historial de cambios de estado de una venta específica.
     */
    @Query("""
        SELECT h FROM HistorialEstado h
        WHERE h.venta.id = :ventaId
        ORDER BY h.cambiadoEn DESC
        """)
    List<HistorialEstado> findByVentaId(@Param("ventaId") UUID ventaId);
 
    /**
     * Actividad reciente: últimos N cambios de estado en el sistema.
     * Útil para el feed de actividad del Back Office.
     */
    @Query("""
        SELECT h FROM HistorialEstado h
        WHERE h.cambiadoEn >= :desde
        ORDER BY h.cambiadoEn DESC
        """)
    List<HistorialEstado> actividadReciente(@Param("desde") OffsetDateTime desde);
}