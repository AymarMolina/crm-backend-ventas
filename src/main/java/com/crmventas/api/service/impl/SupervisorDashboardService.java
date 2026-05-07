package com.crmventas.api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.response.AgenteRendimientoResponse;
import com.crmventas.api.dto.response.AlertaEquipoResponse;
import com.crmventas.api.dto.response.EstadoConteoResponse;
import com.crmventas.api.dto.response.ResumenSupervisorResponse;
import com.crmventas.api.dto.response.TendenciaDiaResponse;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.repository.ObjetivoRepository;
import com.crmventas.api.repository.UsuarioRepository;
import com.crmventas.api.repository.VentaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SupervisorDashboardService {
 
    private final VentaRepository    ventaRepository;
    private final UsuarioRepository  usuarioRepository;
    private final ObjetivoRepository objetivoRepository;
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private Usuario getSupervisorAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
 
    private LocalDate resolverFechaInicio(String periodo) {
        LocalDate hoy = LocalDate.now();
        return switch (periodo) {
            case "7d"  -> hoy.minusDays(7);
            case "mes" -> hoy.withDayOfMonth(1);
            default    -> hoy.minusDays(15);
        };
    }
 
    /**
     * Obtiene los agentes del supervisor autenticado.
     * Toda la lógica del supervisor parte de esta lista.
     */
    private List<Usuario> getAgentesDelSupervisor() {
        UUID supervisorId = getSupervisorAutenticado().getId();
        return usuarioRepository.findAgentesBySupervisor(supervisorId);
    }
 
    private List<UUID> getAgentesIds() {
        return getAgentesDelSupervisor().stream()
                .map(Usuario::getId)
                .toList();
    }
 
    // ── Métodos públicos ──────────────────────────────────────────────────────
 
    /**
     * GET /api/supervisor/dashboard/resumen?periodo=15d
     * KPIs globales del equipo: agentes, ventas, objetivo, monto, comisión, alertas.
     */
    public ResumenSupervisorResponse getResumenEquipo(String periodo) {
        List<Usuario> agentes = getAgentesDelSupervisor();
        List<UUID> ids        = agentes.stream().map(Usuario::getId).toList();
        LocalDate desde       = resolverFechaInicio(periodo);
 
        if (ids.isEmpty()) {
            return ResumenSupervisorResponse.builder()
                    .totalAgentes(0).ventasEquipo(0).objetivoEquipo(0)
                    .montoEquipo(BigDecimal.ZERO).comisionEquipo(BigDecimal.ZERO)
                    .alertasEquipo(0).build();
        }
 
        // Sumamos métricas de todos los agentes
        int    totalVentas   = 0;
        int    totalObjetivo = 0;
        BigDecimal totalMonto    = BigDecimal.ZERO;
        BigDecimal totalComision = BigDecimal.ZERO;
        int    totalAlertas  = 0;
 
        for (Usuario agente : agentes) {
            UUID aid = agente.getId();
 
            totalVentas   += ventaRepository.countVentasActivasPorAgente(aid, desde);
            totalAlertas  += ventaRepository.countAlertasPorAgente(aid);
            totalObjetivo += objetivoRepository.findObjetivoActualPorAgente(aid).orElse(0);
 
            var m = ventaRepository.sumMontoYComisionPorAgente(aid, desde);
            if (m != null) {
                if (m.getMontoTotal()    != null) totalMonto    = totalMonto.add(m.getMontoTotal());
                if (m.getComisionTotal() != null) totalComision = totalComision.add(m.getComisionTotal());
            }
        }
 
        return ResumenSupervisorResponse.builder()
                .totalAgentes(agentes.size())
                .ventasEquipo(totalVentas)
                .objetivoEquipo(totalObjetivo)
                .montoEquipo(totalMonto)
                .comisionEquipo(totalComision)
                .alertasEquipo(totalAlertas)
                .build();
    }
 
    /**
     * GET /api/supervisor/dashboard/agentes?periodo=15d
     * Rendimiento individual de cada agente del equipo (para ranking y tabla).
     */
    public List<AgenteRendimientoResponse> getRendimientoAgentes(String periodo) {
        List<Usuario> agentes = getAgentesDelSupervisor();
        LocalDate desde       = resolverFechaInicio(periodo);
 
        return agentes.stream().map(agente -> {
            UUID aid = agente.getId();
 
            int ventas   = ventaRepository.countVentasActivasPorAgente(aid, desde);
            int objetivo = objetivoRepository.findObjetivoActualPorAgente(aid).orElse(0);
            int alertas  = ventaRepository.countAlertasPorAgente(aid);
 
            var m = ventaRepository.sumMontoYComisionPorAgente(aid, desde);
            BigDecimal monto    = m != null && m.getMontoTotal()    != null ? m.getMontoTotal()    : BigDecimal.ZERO;
            BigDecimal comision = m != null && m.getComisionTotal() != null ? m.getComisionTotal() : BigDecimal.ZERO;
 
            double pct = objetivo > 0 ? Math.min((double) ventas / objetivo * 100, 100) : 0;
 
            return AgenteRendimientoResponse.builder()
                    .agenteId(aid)
                    .nombre(agente.getNombres() + " " + agente.getApellidos())
                    .email(agente.getEmail())
                    .ventasActivas(ventas)
                    .objetivo(objetivo)
                    .pctAlcance(Math.round(pct * 100.0) / 100.0)
                    .montoTotal(monto)
                    .comisionTotal(comision)
                    .alertas(alertas)
                    .build();
        }).toList();
    }
 
    /**
     * GET /api/supervisor/dashboard/tendencia?periodo=15d
     * Tendencia diaria de ventas (monto) de todo el equipo.
     */
    public List<TendenciaDiaResponse> getTendenciaEquipo(String periodo) {
        List<UUID> ids  = getAgentesIds();
        LocalDate desde = resolverFechaInicio(periodo);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
 
        if (ids.isEmpty()) return List.of();
 
        return ventaRepository.tendenciaDiariaEquipo(ids, desde)
                .stream()
                .map(row -> TendenciaDiaResponse.builder()
                        .fecha(row.getFecha().format(fmt))
                        .monto(row.getMonto())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/supervisor/dashboard/por-estado?periodo=15d
     * Distribución de ventas por estado de todo el equipo.
     */
    public List<EstadoConteoResponse> getEstadosEquipo(String periodo) {
        List<UUID> ids  = getAgentesIds();
        LocalDate desde = resolverFechaInicio(periodo);
 
        if (ids.isEmpty()) return List.of();
 
        return ventaRepository.ventasPorEstadoEquipo(ids, desde)
                .stream()
                .map(row -> EstadoConteoResponse.builder()
                        .estado(row.getEstado())
                        .codigo(row.getCodigo())
                        .total(row.getTotal())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/supervisor/dashboard/alertas
     * Todas las alertas activas del equipo (sin filtro de período).
     */
    public List<AlertaEquipoResponse> getAlertasEquipo() {
        List<UUID> ids = getAgentesIds();
 
        if (ids.isEmpty()) return List.of();
 
        return ventaRepository.alertasActivasEquipo(ids)
                .stream()
                .map(v -> AlertaEquipoResponse.builder()
                        .ventaId(v.getId())
                        .codigoVenta(v.getCodigoVenta())
                        .clienteNombre(v.getClienteNombre())
                        .alertaDetalle(v.getAlertaDetalle())
                        .estado(v.getEstado().getNombre())
                        .agenteNombre(v.getAgente().getNombres() + " " + v.getAgente().getApellidos())
                        .actualizadoEn(v.getActualizadoEn())
                        .build())
                .toList();
    }
}