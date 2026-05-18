package com.crmventas.api.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.crmventas.api.repository.ClienteRepository;
import com.crmventas.api.entity.Cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ClienteReportService {

    private final ClienteRepository clienteRepository;

    public ClienteReportService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ByteArrayInputStream exportClientesExcel() throws IOException {
        String[] columns = { "Tipo Cliente", "Tipo Doc", "Nro Doc", "Nombre / Contacto", 
            "Apellido Paterno", "Apellido Materno", "Razón Social", "Teléfono", "Email", 
            "Departamento", "Provincia", "Distrito", "Estado"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // --- ESTILOS ---
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Fila de Cabecera (Usando la interfaz explícita de POI)
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- DATOS ---
            List<Cliente> clientes = clienteRepository.findAll();
            int rowIdx = 1;
            
            for (Cliente cliente : clientes) {
                // Al anteponer el paquete completo evitamos el "Type mismatch" definitivamente
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);

                row.createCell(1).setCellValue(cliente.getTipoCliente() != null ? cliente.getTipoCliente() : "");
                row.createCell(2).setCellValue(cliente.getTipoDoc() != null ? cliente.getTipoDoc() : "DNI");
                row.createCell(3).setCellValue(cliente.getNroDoc() != null ? cliente.getNroDoc() : "");
                row.createCell(4).setCellValue(cliente.getNombre() != null ? cliente.getNombre() : "");
                
                // Manejo de nulos de acuerdo a tu Entity
                row.createCell(5).setCellValue(cliente.getApellidoP() != null ? cliente.getApellidoP() : "");
                row.createCell(6).setCellValue(cliente.getApellidoM() != null ? cliente.getApellidoM() : "");
                row.createCell(7).setCellValue(cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "");
                
                row.createCell(8).setCellValue(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                row.createCell(9).setCellValue(cliente.getEmail() != null ? cliente.getEmail() : "");
                row.createCell(10).setCellValue(cliente.getDepartamento() != null ? cliente.getDepartamento() : "");
                row.createCell(11).setCellValue(cliente.getProvincia() != null ? cliente.getProvincia() : "");
                row.createCell(12).setCellValue(cliente.getDistrito() != null ? cliente.getDistrito() : "");
                row.createCell(13).setCellValue(Boolean.TRUE.equals(cliente.getActivo()) ? "Activo" : "Inactivo");
            }

            // Autoajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}