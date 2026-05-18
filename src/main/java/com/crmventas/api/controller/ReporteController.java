package com.crmventas.api.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.dto.ReporteFiltroDTO;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.service.impl.ReporteService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
 
/**
 * GET /api/reportes/asesores/excel?campanaId=...&fechaDesde=...&fechaHasta=...
 *
 * Requiere permiso: módulo EXPORTAR, acción HACER  (solo GERENTE por defecto).
 * El usuario autenticado se inyecta desde el JWT via @AuthenticationPrincipal.
 */
@RestController
@RequestMapping("/reportes/ventas")
public class ReporteController {
 
    private final ReporteService service;

    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }

    // Simulado: en tu proyecto usa tu propio CampanaRepository/Service
    private final CampanaRepository campanaHelper;
 
    public ReporteController(ReporteService service, CampanaRepository campanaHelper) {
        this.service       = service;
        this.campanaHelper = campanaHelper;
    }
 
    /**
     * Descarga el Excel de reporte de asesores.
     *
     * Ejemplo de llamada:
     *   GET /api/reportes/asesores/excel
     *       ?campanaId=81a44b64-0cc0-43ab-bf54-715dd05d99fa
     *       &fechaDesde=2026-05-01
     *       &fechaHasta=2026-05-18
     */
    @GetMapping("/asesores/excel")
    public ResponseEntity<byte[]> descargarReporte(
            @RequestParam("campanaId")  UUID campanaId,    // Spring convierte el string automáticamente
            @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam("fechaHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) throws IOException {

        String campanaNombre = campanaHelper.obtenerNombre(campanaId);// ← ya tipado

        Usuario agente = getUsuarioAutenticado();
        // Construir el DTO de filtro
        ReporteFiltroDTO filtro = new ReporteFiltroDTO();
        filtro.setCampanaId(campanaId);
        filtro.setFechaDesde(fechaDesde);
        filtro.setFechaHasta(fechaHasta);
 
        // Nombre del generador (usa el rol + nombre del JWT)
        String generadoPor = agente.getNombres()+" "+agente.getApellidos();
 
        byte[] excel = service.generarReporte(filtro, campanaNombre, generadoPor);
 
        // Nombre del archivo con fecha
        String filename = "reporte_asesores_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".xlsx";
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excel.length)
                .body(excel);
    }
}
 