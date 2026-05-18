package com.crmventas.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.crmventas.api.dto.ReporteAsesorDTO;
import com.crmventas.api.dto.ReporteSupervisorDTO;

@Repository
public class ReporteRepository {
 
    private final NamedParameterJdbcTemplate jdbc;
 
    public ReporteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // REPORTE ASESORES (ya existía, no se toca)
    // ─────────────────────────────────────────────────────────────────────────
    public List<ReporteAsesorDTO> obtenerReportePorCampana(
            UUID campanaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {
 
        String sql = """
            SELECT
                u.nombres || ' ' || u.apellidos            AS asesor_nombre,
                o.objetivo_ventas                          AS objetivo_ventas,
                o.monto_comision                           AS monto_comision,
                COALESCE(p.nombre, 'Producto sin nombre')  AS producto_nombre,
                COUNT(v.id)                                AS cantidad,
                COALESCE(v.monto, 0)                       AS precio_unitario
            FROM crm.ventas v
            JOIN crm.usuarios u       ON u.id = v.agente_id
            LEFT JOIN crm.productos p ON p.id = v.producto_id
            LEFT JOIN crm.objetivos o ON o.campana_id = v.campana_id AND o.usuario_id = u.id
            WHERE v.campana_id  = :campanaId::uuid
              AND v.eliminado   = FALSE
              AND v.fecha_venta BETWEEN :fechaDesde AND :fechaHasta
            GROUP BY u.id, u.nombres, u.apellidos, o.objetivo_ventas, o.monto_comision, p.nombre, v.monto
            ORDER BY u.apellidos, u.nombres, p.nombre
            """;
 
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("campanaId",  campanaId)
                .addValue("fechaDesde", fechaDesde)
                .addValue("fechaHasta", fechaHasta);
 
        Map<String, ReporteAsesorDTO> porAsesor = new LinkedHashMap<>();
 
        jdbc.query(sql, params, rs -> {
            String asesorNombre = rs.getString("asesor_nombre");
 
            ReporteAsesorDTO dto = porAsesor.computeIfAbsent(asesorNombre, k -> {
                ReporteAsesorDTO nuevoDto = new ReporteAsesorDTO();
                nuevoDto.setAsesorNombre(asesorNombre);
                try {
                    nuevoDto.setObjetivoVentas(rs.getObject("objetivo_ventas") != null
                            ? rs.getInt("objetivo_ventas") : 0);
                    nuevoDto.setMontoComision(rs.getBigDecimal("monto_comision"));
                } catch (Exception e) {
                    nuevoDto.setObjetivoVentas(0);
                    nuevoDto.setMontoComision(BigDecimal.ZERO);
                }
                nuevoDto.setProductos(new ArrayList<>());
                return nuevoDto;
            });
 
            dto.getProductos().add(new ReporteAsesorDTO.FilaProducto(
                    rs.getString("producto_nombre"),
                    rs.getInt("cantidad"),
                    rs.getBigDecimal("precio_unitario")));
        });
 
        return new ArrayList<>(porAsesor.values());
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // REPORTE SUPERVISORES  — mismo patrón JDBC, sin @Query
    // ─────────────────────────────────────────────────────────────────────────
    public List<ReporteSupervisorDTO> obtenerReporteSupervisores(UUID campanaId) {
 
        String sql = """
            SELECT
                c.nombre                                                              AS campana,
                c.mes,
                c.anio,
                CAST(s.id AS VARCHAR)                                                 AS supervisor_id,
                s.nombres || ' ' || s.apellidos                                      AS supervisor,
                os.objetivo_ventas                                                    AS meta_supervisor,
                os.monto_comision                                                     AS comision_max_supervisor,
                CAST(a.id AS VARCHAR)                                                 AS agente_id,
                a.nombres || ' ' || a.apellidos                                      AS agente,
                oa.objetivo_ventas                                                    AS meta_agente,
                oa.monto_comision                                                     AS comision_max_agente,
                COUNT(v.id)                                                           AS ventas_activas,
                COALESCE(SUM(v.monto), 0)                                            AS monto_total,
                COALESCE(SUM(v.comision_generada), 0)                                AS comision_generada,
                ROUND(COUNT(v.id)::NUMERIC / NULLIF(oa.objetivo_ventas, 0) * 100, 2) AS pct_alcance_agente
            FROM crm.campanas c
            JOIN crm.usuarios a  ON a.activo = TRUE
            JOIN crm.roles    ra ON ra.id = a.rol_id  AND ra.codigo = 'AGENTE'
            JOIN crm.usuarios s  ON s.id  = a.supervisor_id
            JOIN crm.roles    rs ON rs.id = s.rol_id  AND rs.codigo = 'SUPERVISOR'
            LEFT JOIN crm.objetivos oa ON oa.usuario_id = a.id AND oa.campana_id = c.id
            LEFT JOIN crm.objetivos os ON os.usuario_id = s.id AND os.campana_id = c.id
            LEFT JOIN crm.ventas v
                ON  v.agente_id  = a.id
                AND v.campana_id = c.id
                AND v.eliminado  = FALSE
                AND v.estado_id NOT IN (
                    SELECT id FROM crm.estados_venta WHERE es_final = TRUE
                )
            WHERE c.id = :campanaId::uuid
            GROUP BY
                c.id, c.nombre, c.mes, c.anio,
                s.id, s.nombres, s.apellidos,
                os.objetivo_ventas, os.monto_comision,
                a.id, a.nombres, a.apellidos,
                oa.objetivo_ventas, oa.monto_comision
            ORDER BY supervisor, agente
            """;
 
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("campanaId", campanaId);
 
        List<ReporteSupervisorDTO> result = new ArrayList<>();
 
        jdbc.query(sql, params, rs -> {
            ReporteSupervisorDTO dto = new ReporteSupervisorDTO();
            dto.setCampana(rs.getString("campana"));
            dto.setMes(rs.getInt("mes"));
            dto.setAnio(rs.getInt("anio"));
            dto.setSupervisorId(rs.getString("supervisor_id"));
            dto.setSupervisor(rs.getString("supervisor"));
            dto.setMetaSupervisor(rs.getObject("meta_supervisor") != null
                    ? rs.getInt("meta_supervisor") : null);
            dto.setComisionMaxSupervisor(rs.getBigDecimal("comision_max_supervisor"));
            dto.setAgenteId(rs.getString("agente_id"));
            dto.setAgente(rs.getString("agente"));
            dto.setMetaAgente(rs.getObject("meta_agente") != null
                    ? rs.getInt("meta_agente") : null);
            dto.setComisionMaxAgente(rs.getBigDecimal("comision_max_agente"));
            dto.setVentasActivas(rs.getLong("ventas_activas"));
            dto.setMontoTotal(rs.getBigDecimal("monto_total"));
            dto.setComisionGenerada(rs.getBigDecimal("comision_generada"));
            dto.setPctAlcanceAgente(rs.getObject("pct_alcance_agente") != null
                    ? rs.getBigDecimal("pct_alcance_agente") : null);
            result.add(dto);
        });
 
        return result;
    }
}
 