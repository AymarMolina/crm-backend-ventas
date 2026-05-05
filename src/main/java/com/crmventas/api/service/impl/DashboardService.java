package com.crmventas.api.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.dto.response.AlertaVentaResponse;
import com.crmventas.api.dto.response.EstadoConteoResponse;
import com.crmventas.api.dto.response.ResumenAsesorResponse;
import com.crmventas.api.dto.response.TendenciaDiaResponse;
import com.crmventas.api.dto.response.VentasPorCampanaResponse;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.repository.ObjetivoRepository;
import com.crmventas.api.repository.VentaRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
 
    private final VentaRepository     ventaRepository;
    private final ObjetivoRepository  objetivoRepository;
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
 
    /**
     * Convierte el string de período al LocalDate de inicio.
     * "7d"  → hace 7 días
     * "15d" → hace 15 días
     * "mes" → primer día del mes actual
     */
    private LocalDate resolverFechaInicio(String periodo) {
        LocalDate hoy = LocalDate.now();
        return switch (periodo) {
            case "7d"  -> hoy.minusDays(7);
            case "mes" -> hoy.withDayOfMonth(1);
            default    -> hoy.minusDays(15);   // "15d" y cualquier otro
        };
    }
 
    // ── Endpoints ─────────────────────────────────────────────────────────────
 
    /**
     * GET /api/ventas/resumen?periodo=15d
     * Devuelve KPIs: ventas activas, objetivo, monto total, comisión, alertas.
     */
    public ResumenAsesorResponse getResumen(String periodo) {
        UUID agenteId   = getUsuarioAutenticado().getId();
        LocalDate desde = resolverFechaInicio(periodo);
 
        int    ventasActivas    = ventaRepository.countVentasActivas(agenteId, desde);
        var    montos           = ventaRepository.sumMontoYComision(agenteId, desde);
        int    alertas          = ventaRepository.countAlertas(agenteId);
 
        // Objetivo del mes actual (campaña activa asignada al agente)
        int objetivo = objetivoRepository
                .findObjetivoActualPorAgente(agenteId, LocalDate.now().getMonthValue(),
                                             LocalDate.now().getYear())
                .orElse(0);
 
        return ResumenAsesorResponse.builder()
                .ventasActivas(ventasActivas)
                .objetivo(objetivo)
                .montoTotal(montos.getMontoTotal())
                .comisionEstimada(montos.getComisionTotal())
                .alertas(alertas)
                .build();
    }
 
    /**
     * GET /api/ventas/tendencia?periodo=15d
     * Devuelve monto total agrupado por día.
     */
    public List<TendenciaDiaResponse> getTendencia(String periodo) {
        UUID agenteId   = getUsuarioAutenticado().getId();
        LocalDate desde = resolverFechaInicio(periodo);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
 
        return ventaRepository.tendenciaDiaria(agenteId, desde)
                .stream()
                .map(row -> TendenciaDiaResponse.builder()
                        .fecha(row.getFecha().format(fmt))
                        .monto(row.getMonto())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/ventas/por-campana?periodo=15d
     * Devuelve conteo de ventas agrupado por línea/campaña.
     */
    public List<VentasPorCampanaResponse> getPorCampana(String periodo) {
        UUID agenteId   = getUsuarioAutenticado().getId();
        LocalDate desde = resolverFechaInicio(periodo);
 
        return ventaRepository.ventasPorCampana(agenteId, desde)
                .stream()
                .map(row -> VentasPorCampanaResponse.builder()
                        .campana(row.getCampana())
                        .total(row.getTotal())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/ventas/por-estado?periodo=15d
     * Devuelve conteo de ventas agrupado por estado.
     */
    public List<EstadoConteoResponse> getPorEstado(String periodo) {
        UUID agenteId   = getUsuarioAutenticado().getId();
        LocalDate desde = resolverFechaInicio(periodo);
 
        return ventaRepository.ventasPorEstado(agenteId, desde)
                .stream()
                .map(row -> EstadoConteoResponse.builder()
                        .estado(row.getEstado())
                        .codigo(row.getCodigo())
                        .total(row.getTotal())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/ventas/alertas
     * Devuelve las ventas observadas del asesor autenticado.
     * Sin filtro de período: siempre muestra todas las alertas activas.
     */
    public List<AlertaVentaResponse> getAlertas() {
        UUID agenteId = getUsuarioAutenticado().getId();
 
        return ventaRepository.alertasActivas(agenteId)
                .stream()
                .map(v -> AlertaVentaResponse.builder()
                        .id(v.getId())
                        .codigoVenta(v.getCodigoVenta())
                        .clienteNombre(v.getClienteNombre())
                        .alertaDetalle(v.getAlertaDetalle())
                        .estado(v.getEstado().getNombre())
                        .actualizadoEn(v.getActualizadoEn())
                        .build())
                .toList();
    }
}