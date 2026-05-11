package com.crmventas.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.dto.FiltroReporteDTO;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.service.impl.ReporteVentasService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
 
@RestController
@RequestMapping("reportes/ventas")
@RequiredArgsConstructor
public class ReporteVentasController {

    private final ReporteVentasService reporteService;

    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }

    // --- ASESOR ---
    @GetMapping("/asesor")
    @PreAuthorize("hasAnyRole('AGENTE','ASESOR')")
    public ResponseEntity<byte[]> reporteAsesor(
            @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam("fechaHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(value = "campanaId", required = false) UUID campanaId,
            @RequestParam(value = "campanaNombre", required = false) String campanaNombre
    ) throws IOException {

        FiltroReporteDTO filtro = FiltroReporteDTO.builder()
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .campanaId(campanaId)
                .campanaNombre(campanaNombre)
                .build();

        Usuario agente = getUsuarioAutenticado();
        byte[] excel = reporteService.generarReporteAsesor(agente.getId(), filtro);
        return excelResponse(excel, "reporte_ventas_asesor");
    }

    // --- SUPERVISOR ---
    @GetMapping("/supervisor")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<byte[]> reporteSupervisor(
            @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam("fechaHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(value = "campanaId", required = false) UUID campanaId,
            @RequestParam(value = "campanaNombre", required = false) String campanaNombre
    ) throws IOException {

        FiltroReporteDTO filtro = FiltroReporteDTO.builder()
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .campanaId(campanaId)
                .campanaNombre(campanaNombre)
                .build();

        Usuario agente = getUsuarioAutenticado();
        byte[] excel = reporteService.generarReporteSupervisor(agente.getId(), filtro);
        return excelResponse(excel, "reporte_ventas_supervisor");
    }

    // --- GERENTE ---
    @GetMapping("/gerente")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<byte[]> reporteGerente(
            @RequestParam("fechaDesde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam("fechaHasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(value = "campanaId", required = false) UUID campanaId,
            @RequestParam(value = "campanaNombre", required = false) String campanaNombre,
            @RequestParam(value = "supervisorId", required = false) UUID supervisorId
    ) throws IOException {

        FiltroReporteDTO filtro = FiltroReporteDTO.builder()
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .campanaId(campanaId)
                .campanaNombre(campanaNombre)
                .supervisorId(supervisorId)
                .build();

        byte[] excel = reporteService.generarReporteGerente(filtro);
        return excelResponse(excel, "reporte_ventas_gerente");
    }

    private ResponseEntity<byte[]> excelResponse(byte[] content, String baseFilename) {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = baseFilename + "_" + fecha + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(content);
    }
}