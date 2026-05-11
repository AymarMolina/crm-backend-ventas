package com.crmventas.api.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.crmventas.api.dto.FiltroReporteDTO;
import com.crmventas.api.dto.VentaReporteDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
@Repository
@RequiredArgsConstructor
public class Ventareporterepository {
 
    private final EntityManager em;
 
    // ─────────────────────────────────────────────────────────────────────────
    // Consulta principal con filtros dinámicos
    // ─────────────────────────────────────────────────────────────────────────
    public List<VentaReporteDTO> buscarVentasFiltradas(FiltroReporteDTO f) {
 
        StringBuilder jpql = new StringBuilder("""
            SELECT new com.crmventas.api.dto.VentaReporteDTO(
                v.codigoVenta,
                v.fechaVenta,
                c.nombre,
                lp.nombre,
                (agente.nombres || ' ' || agente.apellidos),
                CASE WHEN sup.id IS NOT NULL THEN (sup.nombres || ' ' || sup.apellidos) ELSE NULL END,
                COALESCE(cl.nombre || ' ' || COALESCE(cl.apellidoP,'') || ' ' || COALESCE(cl.apellidoM,''), v.clienteNombre),
                COALESCE(cl.nroDoc, v.clienteDoc),
                COALESCE(cl.telefono, v.clienteTelefono),
                v.monto,
                v.comisionPorcentaje,
                v.comisionGenerada,
                ev.codigo,
                v.tieneAlerta,
                v.alertaDetalle
            )
            FROM Venta v
            JOIN v.campana c
            JOIN c.linea lp
            JOIN v.agente agente
            LEFT JOIN agente.supervisor sup
            LEFT JOIN v.cliente cl
            JOIN v.estado ev
            WHERE v.eliminado = false
        """);
 
        List<String> conditions = new ArrayList<>();
 
        // Filtro por agentes específicos (asesor o equipo de supervisor)
        if (f.getAgenteId() != null) {
            conditions.add("agente.id = :agenteId");
        }
        if (f.getAgenteIds() != null && !f.getAgenteIds().isEmpty()) {
            conditions.add("agente.id IN :agenteIds");
        }
        if (f.getSupervisorId() != null && f.getAgenteIds() == null) {
            conditions.add("(sup.id = :supervisorId OR agente.id = :supervisorId)");
        }
        // Filtros de fecha
        if (f.getFechaDesde() != null) {
            conditions.add("v.fechaVenta >= :fechaDesde");
        }
        if (f.getFechaHasta() != null) {
            conditions.add("v.fechaVenta <= :fechaHasta");
        }
        // Filtro campaña
        if (f.getCampanaId() != null) {
            conditions.add("c.id = :campanaId");
        }
 
        if (!conditions.isEmpty()) {
            jpql.append(" AND ").append(String.join(" AND ", conditions));
        }
        jpql.append(" ORDER BY v.fechaVenta DESC, agente.apellidos ASC");
 
        TypedQuery<VentaReporteDTO> q = em.createQuery(jpql.toString(), VentaReporteDTO.class);
 
        if (f.getAgenteId() != null)       q.setParameter("agenteId", f.getAgenteId());
        if (f.getAgenteIds() != null && !f.getAgenteIds().isEmpty())
                                            q.setParameter("agenteIds", f.getAgenteIds());
        if (f.getSupervisorId() != null && f.getAgenteIds() == null)
                                            q.setParameter("supervisorId", f.getSupervisorId());
        if (f.getFechaDesde() != null)      q.setParameter("fechaDesde", f.getFechaDesde());
        if (f.getFechaHasta() != null)      q.setParameter("fechaHasta", f.getFechaHasta());
        if (f.getCampanaId() != null)       q.setParameter("campanaId", f.getCampanaId());
 
        return q.getResultList();
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // Obtener IDs de asesores bajo un supervisor
    // ─────────────────────────────────────────────────────────────────────────
    public List<UUID> obtenerAsesorDesSupervisor(UUID supervisorId) {
        return em.createQuery(
                "SELECT u.id FROM Usuario u WHERE u.supervisor.id = :supervisorId AND u.activo = true",
                UUID.class)
            .setParameter("supervisorId", supervisorId)
            .getResultList();
    }
}
