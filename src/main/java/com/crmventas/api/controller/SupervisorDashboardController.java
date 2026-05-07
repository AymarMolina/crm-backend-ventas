package com.crmventas.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.dto.response.AgenteRendimientoResponse;
import com.crmventas.api.dto.response.AlertaEquipoResponse;
import com.crmventas.api.dto.response.EstadoConteoResponse;
import com.crmventas.api.dto.response.ResumenSupervisorResponse;
import com.crmventas.api.dto.response.TendenciaDiaResponse;
import com.crmventas.api.service.impl.SupervisorDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/supervisor/dashboard")
@RequiredArgsConstructor
public class SupervisorDashboardController {
 
    private final SupervisorDashboardService supervisorDashboardService;
 
    /**
     * GET /api/supervisor/dashboard/resumen?periodo=15d
     * KPIs globales del equipo: agentes, ventas, objetivo grupal, monto, comisión, alertas.
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenSupervisorResponse> resumen(
        @RequestParam(name = "periodo", defaultValue = "15d") String periodo
    ) {
        return ResponseEntity.ok(supervisorDashboardService.getResumenEquipo(periodo));
    }
 
    /**
     * GET /api/supervisor/dashboard/agentes?periodo=15d
     * Rendimiento individual de cada agente: ventas, objetivo, % alcance, monto, comisión, alertas.
     * Úsalo para el ranking y la tabla del equipo.
     */
    @GetMapping("/agentes")
    public ResponseEntity<List<AgenteRendimientoResponse>> agentes(
        @RequestParam(name = "periodo", defaultValue = "15d") String periodo
    ) {
        return ResponseEntity.ok(supervisorDashboardService.getRendimientoAgentes(periodo));
    }
 
    /**
     * GET /api/supervisor/dashboard/tendencia?periodo=15d
     * Tendencia diaria de monto total del equipo completo.
     */
    @GetMapping("/tendencia")
    public ResponseEntity<List<TendenciaDiaResponse>> tendencia(
        @RequestParam(name = "periodo", defaultValue = "15d") String periodo
    ) {
        return ResponseEntity.ok(supervisorDashboardService.getTendenciaEquipo(periodo));
    }
 
    /**
     * GET /api/supervisor/dashboard/por-estado?periodo=15d
     * Distribución de ventas por estado de todo el equipo.
     */
    @GetMapping("/por-estado")
    public ResponseEntity<List<EstadoConteoResponse>> porEstado(
        @RequestParam(name = "periodo", defaultValue = "15d") String periodo
    ) {
        return ResponseEntity.ok(supervisorDashboardService.getEstadosEquipo(periodo));
    }
 
    /**
     * GET /api/supervisor/dashboard/alertas
     * Todas las alertas activas del equipo (sin filtro de período).
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaEquipoResponse>> alertas() {
        return ResponseEntity.ok(supervisorDashboardService.getAlertasEquipo());
    }
}