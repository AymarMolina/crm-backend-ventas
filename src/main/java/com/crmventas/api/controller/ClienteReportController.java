package com.crmventas.api.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.service.impl.ClienteReportService;

import java.io.IOException;

@RestController
@RequestMapping("/reporte")
public class ClienteReportController {

    private final ClienteReportService reportService;

    public ClienteReportController(ClienteReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/excel")
    public ResponseEntity<Resource> getReporteClientes() {
        String filename = "reporte_clientes.xlsx";
        try {
            InputStreamResource file = new InputStreamResource(reportService.exportClientesExcel());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}