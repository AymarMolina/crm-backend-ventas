package com.crmventas.api.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.dto.ReporteSupervisorDTO;
import com.crmventas.api.service.impl.ReporteSupervisorService;

import java.io.IOException;
import java.util.List;
 
@RestController
@RequestMapping("/reportes/supervisores")
public class ReporteSupervisorController {
 
    private final ReporteSupervisorService service;
 
    public ReporteSupervisorController(ReporteSupervisorService service) {
        this.service = service;
    }
 
    @GetMapping("/{campanaId}")
    @PreAuthorize("hasAnyRole('GERENTE', 'SUPERVISOR')")
    public ResponseEntity<List<ReporteSupervisorDTO>> obtenerReporte(
            @PathVariable("campanaId") String campanaId) {
        return ResponseEntity.ok(service.obtenerReporte(campanaId));
    }
 
    @GetMapping("/{campanaId}/excel")
    @PreAuthorize("hasAnyRole('GERENTE', 'SUPERVISOR')")
    public ResponseEntity<byte[]> descargarExcel(
            @PathVariable("campanaId") String campanaId) throws IOException {
 
        byte[] excel = service.generarExcel(campanaId);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"reporte-supervisores.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}