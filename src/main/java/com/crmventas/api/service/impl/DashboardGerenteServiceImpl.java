package com.crmventas.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.response.AgenteRendimientoResponsedash;
import com.crmventas.api.dto.response.AlertaResponse;
import com.crmventas.api.dto.response.CampanaResponse;
import com.crmventas.api.dto.response.CampanaSelectorResponse;
import com.crmventas.api.dto.response.DashboardGerenteResponse;
import com.crmventas.api.exception.ResourceNotFoundException;
import com.crmventas.api.service.DashboardGerenteService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardGerenteServiceImpl implements DashboardGerenteService {
 
    private final NamedParameterJdbcTemplate jdbc;
 
    // ─────────────────────────────────────────────────────────────────────────
    // 1. Selector de campañas
    // ─────────────────────────────────────────────────────────────────────────
 
    @Override
    public List<CampanaSelectorResponse> getCampanasActivas() {
        String sql = """
            SELECT c.id::text, c.nombre, lp.nombre AS linea_nombre, c.mes, c.anio
            FROM crm.campanas c
            JOIN crm.lineas_producto lp ON lp.id = c.linea_id
            WHERE c.activo = TRUE
            ORDER BY c.anio DESC, c.mes DESC, lp.nombre
            """;
 
        return jdbc.query(sql, Map.of(), (rs, i) -> new CampanaSelectorResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("nombre"),
                rs.getString("linea_nombre"),
                rs.getInt("mes"),
                rs.getInt("anio")
        ));
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // 2. Dashboard completo por campaña
    // ─────────────────────────────────────────────────────────────────────────
 
    @Override
    public DashboardGerenteResponse getDashboard(UUID campanaId) {
        var params = new MapSqlParameterSource("campanaId", campanaId);
 
        // 2a. Cabecera de la campaña
        CampanaResponse campana = findCampana(campanaId, params);
 
        // 2b. Métricas globales
        Map<String, Object> metricas = findMetricas(params);
        int ventasActivas   = toInt(metricas.get("ventas_activas"));
        int caidas          = toInt(metricas.get("caidas"));
        int alertasActivas  = toInt(metricas.get("alertas_activas"));
        BigDecimal comision = toBigDecimal(metricas.get("comision_estimada"));
 
        // 2c. Objetivo total de agentes y conteo
        int objetivoCampana  = sumObjetivoAgentes(params);
        int agentesTotal     = countAgentesConObjetivo(params);
 
        // 2d. Agentes con al menos 1 venta activa
        int agentesActivos = countAgentesActivos(params);
 
        // 2e. Porcentajes derivados
        double pctAlcance = objetivoCampana > 0
                ? round((double) ventasActivas / objetivoCampana * 100)
                : 0.0;
 
        double tasaCaida = (ventasActivas + caidas) > 0
                ? round((double) caidas / (ventasActivas + caidas) * 100)
                : 0.0;
 
        // 2f. Distribución por estado
        Map<String, Integer> distribucion = findDistribucionEstados(params);
 
        // 2g. Ranking de agentes
        List<AgenteRendimientoResponsedash> agentes = findAgentes(params);
 
        // 2h. Últimas alertas
        List<AlertaResponse> alertas = findAlertas(params);
 
        return new DashboardGerenteResponse(
                campana,
                ventasActivas,
                objetivoCampana,
                pctAlcance,
                agentesActivos,
                agentesTotal,
                alertasActivas,
                caidas,
                tasaCaida,
                comision,
                distribucion,
                agentes,
                alertas
        );
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // Métodos privados de consulta
    // ─────────────────────────────────────────────────────────────────────────
 
    private CampanaResponse findCampana(UUID campanaId, MapSqlParameterSource params) {
        String sql = """
            SELECT c.id::text, c.nombre, lp.nombre AS linea_nombre,
                   c.mes, c.anio, c.objetivo_total, c.activo
            FROM crm.campanas c
            JOIN crm.lineas_producto lp ON lp.id = c.linea_id
            WHERE c.id = :campanaId
            """;
 
        return jdbc.query(sql, params, (rs, i) -> new CampanaResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("nombre"),
                rs.getString("linea_nombre"),
                rs.getInt("mes"),
                rs.getInt("anio"),
                rs.getInt("objetivo_total"),
                rs.getBoolean("activo")
        )).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada: " + campanaId));
    }
 
    private Map<String, Object> findMetricas(MapSqlParameterSource params) {
        String sql = """
            SELECT
                COUNT(*) FILTER (WHERE ev.es_final = FALSE AND v.eliminado = FALSE)  AS ventas_activas,
                COUNT(*) FILTER (WHERE ev.codigo = 'CAIDA'  AND v.eliminado = FALSE)  AS caidas,
                COUNT(*) FILTER (WHERE v.tiene_alerta = TRUE AND v.eliminado = FALSE) AS alertas_activas,
                COALESCE(SUM(v.comision_generada)
                    FILTER (WHERE ev.es_final = FALSE AND v.eliminado = FALSE), 0)    AS comision_estimada
            FROM crm.ventas v
            JOIN crm.estados_venta ev ON ev.id = v.estado_id
            WHERE v.campana_id = :campanaId
            """;
        return jdbc.queryForMap(sql, params);
    }
 
    private int sumObjetivoAgentes(MapSqlParameterSource params) {
        String sql = "SELECT COALESCE(SUM(objetivo_ventas), 0) FROM crm.objetivos WHERE campana_id = :campanaId";
        Integer r = jdbc.queryForObject(sql, params, Integer.class);
        return r != null ? r : 0;
    }
 
    private int countAgentesConObjetivo(MapSqlParameterSource params) {
        String sql = """
            SELECT COUNT(DISTINCT o.usuario_id)
            FROM crm.objetivos o
            JOIN crm.usuarios u ON u.id = o.usuario_id AND u.activo = TRUE
            WHERE o.campana_id = :campanaId
            """;
        Integer r = jdbc.queryForObject(sql, params, Integer.class);
        return r != null ? r : 0;
    }
 
    private int countAgentesActivos(MapSqlParameterSource params) {
        String sql = """
            SELECT COUNT(DISTINCT v.agente_id)
            FROM crm.ventas v
            JOIN crm.estados_venta ev ON ev.id = v.estado_id
            WHERE v.campana_id   = :campanaId
              AND ev.es_final    = FALSE
              AND v.eliminado    = FALSE
            """;
        Integer r = jdbc.queryForObject(sql, params, Integer.class);
        return r != null ? r : 0;
    }
 
    private Map<String, Integer> findDistribucionEstados(MapSqlParameterSource params) {
        String sql = """
            SELECT ev.codigo AS estado_codigo, COUNT(v.id) AS cantidad
            FROM crm.estados_venta ev
            LEFT JOIN crm.ventas v
                   ON v.estado_id  = ev.id
                  AND v.campana_id = :campanaId
                  AND v.eliminado  = FALSE
            GROUP BY ev.id, ev.codigo, ev.orden
            ORDER BY ev.orden
            """;
 
        Map<String, Integer> mapa = new LinkedHashMap<>();
        jdbc.query(sql, params, rs -> {
            mapa.put(rs.getString("estado_codigo"), rs.getInt("cantidad"));
        });
        return mapa;
    }
 
    private List<AgenteRendimientoResponsedash> findAgentes(MapSqlParameterSource params) {
        String sql = """
            SELECT
                u.id::text                                                          AS agente_id,
                u.nombres || ' ' || u.apellidos                  AS nombre_completo,
                COALESCE(sup.nombres || ' ' || sup.apellidos, '') AS supervisor_nombre,
                o.objetivo_ventas,
                o.monto_comision                                                    AS monto_comision_max,
                COUNT(v.id) FILTER (
                    WHERE ev.es_final = FALSE AND v.eliminado = FALSE)              AS ventas_activas,
                COALESCE(SUM(v.comision_generada) FILTER (
                    WHERE ev.es_final = FALSE AND v.eliminado = FALSE), 0)          AS comision_estimada,
                COUNT(v.id) FILTER (
                    WHERE ev.codigo = 'CAIDA' AND v.eliminado = FALSE)              AS caidas,
                COUNT(v.id) FILTER (
                    WHERE v.tiene_alerta = TRUE AND v.eliminado = FALSE)            AS alertas
            FROM crm.objetivos o
            JOIN crm.usuarios u         ON u.id  = o.usuario_id AND u.activo = TRUE
            JOIN crm.roles r            ON r.id  = u.rol_id     AND r.codigo = 'AGENTE'
            LEFT JOIN crm.usuarios sup  ON sup.id = u.supervisor_id
            LEFT JOIN crm.ventas v      ON v.agente_id  = u.id
                                      AND v.campana_id  = o.campana_id
            LEFT JOIN crm.estados_venta ev ON ev.id = v.estado_id
            WHERE o.campana_id = :campanaId
            GROUP BY u.id, u.nombres, u.apellidos,
                        sup.nombres, sup.apellidos,
                     o.objetivo_ventas, o.monto_comision
            ORDER BY ventas_activas DESC
            """;
 
        return jdbc.query(sql, params, (rs, i) -> {
            int ventasActivas   = rs.getInt("ventas_activas");
            int objetivo        = rs.getInt("objetivo_ventas");
            double pct = objetivo > 0 ? round((double) ventasActivas / objetivo * 100) : 0.0;
 
            return new AgenteRendimientoResponsedash(
                    UUID.fromString(rs.getString("agente_id")),
                    rs.getString("nombre_completo"),
                    rs.getString("supervisor_nombre"),
                    ventasActivas,
                    objetivo,
                    pct,
                    toBigDecimal(rs.getObject("comision_estimada")),
                    toBigDecimal(rs.getObject("monto_comision_max")),
                    rs.getInt("caidas"),
                    rs.getInt("alertas")
            );
        });
    }
 
    private List<AlertaResponse> findAlertas(MapSqlParameterSource params) {
        String sql = """
            SELECT
                v.id::text                                     AS venta_id,
                v.codigo_venta,
                v.alerta_detalle,
                ev.nombre                                      AS estado_nombre,
                u.nombres || ' ' || u.apellidos                AS agente_nombre,
                v.actualizado_en
            FROM crm.ventas v
            JOIN crm.estados_venta ev ON ev.id = v.estado_id
            JOIN crm.usuarios      u  ON u.id  = v.agente_id
            WHERE v.campana_id   = :campanaId
              AND v.tiene_alerta = TRUE
              AND v.eliminado    = FALSE
            ORDER BY v.actualizado_en DESC
            LIMIT 10
            """;
 
        return jdbc.query(sql, params, (rs, i) -> new AlertaResponse(
                UUID.fromString(rs.getString("venta_id")),
                rs.getString("codigo_venta"),
                rs.getString("alerta_detalle"),
                rs.getString("estado_nombre"),
                rs.getString("agente_nombre"),
                rs.getObject("actualizado_en", OffsetDateTime.class)
        ));
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de conversión
    // ─────────────────────────────────────────────────────────────────────────
 
    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
 
    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }
 
    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}