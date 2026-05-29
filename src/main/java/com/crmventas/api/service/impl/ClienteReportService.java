package com.crmventas.api.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.crmventas.api.repository.ClienteRepository;
import com.crmventas.api.entity.Cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

            // --- 1. INSERTAR LOGO CON MÁRGENES ---
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();

                    // ¡LOGO MÁS GRANDE!
                    // Ahora ocupa desde la Columna 0 a la 3 (A, B, C, D) y Fila 0 a la 4 (1, 2, 3, 4, 5)
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(3); // Antes era 2
                    anchor.setRow2(4); // Antes era 3

                    // Mantengo los mismos márgenes para que respire bien
                    anchor.setDx1(Units.toEMU(15));
                    anchor.setDy1(Units.toEMU(15));
                    anchor.setDx2(-Units.toEMU(15));
                    anchor.setDy2(-Units.toEMU(15));

                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor, pictureIdx);
                } else {
                    System.out.println("Advertencia: No se encontró logo.png");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar el logo: " + e.getMessage());
            }

            // --- 2. TÍTULO DEL REPORTE ---
            // Como el logo es más grande, movemos el título a la Columna E (Índice 4)
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(1); 
            Cell titleCell = titleRow.createCell(4); 
            titleCell.setCellValue("REPORTE DE CLIENTES");

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18); 
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex()); 

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER); 
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); 
            titleCell.setCellStyle(titleStyle);

            // Combinamos las celdas para el título haciéndolo también más alto (Filas 1 a 3, Columnas 4 a 9)
            sheet.addMergedRegion(new CellRangeAddress(1, 3, 4, 9));

            // --- 3. ESTILOS DE CABECERA ---
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- 4. FILA DE CABECERA ---
            // Como el logo es más alto, empujamos la tabla hasta la fila 6 (índice 5)
            int startRowIndex = 5; 
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(startRowIndex);
            
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- 5. DATOS ---
            List<Cliente> clientes = clienteRepository.findAll();
            int rowIdx = startRowIndex + 1; 
            
            for (Cliente cliente : clientes) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(cliente.getTipoCliente() != null ? cliente.getTipoCliente() : "");
                row.createCell(1).setCellValue(cliente.getTipoDoc() != null ? cliente.getTipoDoc() : "DNI");
                row.createCell(2).setCellValue(cliente.getNroDoc() != null ? cliente.getNroDoc() : "");
                row.createCell(3).setCellValue(cliente.getNombre() != null ? cliente.getNombre() : "");
                row.createCell(4).setCellValue(cliente.getApellidoP() != null ? cliente.getApellidoP() : "");
                row.createCell(5).setCellValue(cliente.getApellidoM() != null ? cliente.getApellidoM() : "");
                row.createCell(6).setCellValue(cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "");
                row.createCell(7).setCellValue(cliente.getTelefono() != null ? cliente.getTelefono() : "");
                row.createCell(8).setCellValue(cliente.getEmail() != null ? cliente.getEmail() : "");
                row.createCell(9).setCellValue(cliente.getDepartamento() != null ? cliente.getDepartamento() : "");
                row.createCell(10).setCellValue(cliente.getProvincia() != null ? cliente.getProvincia() : "");
                row.createCell(11).setCellValue(cliente.getDistrito() != null ? cliente.getDistrito() : "");
                row.createCell(12).setCellValue(Boolean.TRUE.equals(cliente.getActivo()) ? "Activo" : "Inactivo");
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