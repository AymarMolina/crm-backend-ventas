package com.crmventas.api.service.impl;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.ReporteSupervisorDTO;
import com.crmventas.api.repository.ReporteRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
 
@Service
public class ReporteSupervisorService {
 
    private final ReporteRepository reporteRepository;
 
    public ReporteSupervisorService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }
 
    // ── JSON ──────────────────────────────────────────────────────────────────
    public List<ReporteSupervisorDTO> obtenerReporte(String campanaId) {
        return reporteRepository.obtenerReporteSupervisores(UUID.fromString(campanaId));
    }
 
    // ── Excel ─────────────────────────────────────────────────────────────────
    public byte[] generarExcel(String campanaId) throws IOException {
        List<ReporteSupervisorDTO> datos = obtenerReporte(campanaId);
 
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
 
            XSSFSheet sheet = wb.createSheet("Reporte Supervisores");
            int[] colWidths = { 5000, 5500, 2500, 3500, 5500, 2500, 3500, 2800, 3500, 3500, 3000 };
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i]);
            }
 
            // ── Estilos ──────────────────────────────────────────────────────
            XSSFFont fTitulo = wb.createFont();
            fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 13);
            XSSFCellStyle csTitulo = wb.createCellStyle();
            csTitulo.setFont(fTitulo);
            csTitulo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            csTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csTitulo.setAlignment(HorizontalAlignment.CENTER);
 
            XSSFFont fHeader = wb.createFont();
            fHeader.setBold(true);
            fHeader.setColor(IndexedColors.WHITE.getIndex());
            XSSFCellStyle csHeader = wb.createCellStyle();
            csHeader.setFont(fHeader);
            csHeader.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csHeader.setAlignment(HorizontalAlignment.CENTER);
            csHeader.setBorderBottom(BorderStyle.THIN);
 
            XSSFCellStyle csDato = wb.createCellStyle();
            csDato.setBorderBottom(BorderStyle.HAIR);
            csDato.setBorderLeft(BorderStyle.HAIR);
            csDato.setBorderRight(BorderStyle.HAIR);
 
            XSSFCellStyle csNum = wb.createCellStyle();
            csNum.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            csNum.setBorderBottom(BorderStyle.HAIR);
            csNum.setBorderLeft(BorderStyle.HAIR);
            csNum.setBorderRight(BorderStyle.HAIR);
 
            // ── Fila título ───────────────────────────────────────────────────
            String campana = datos.isEmpty() ? "-" : datos.get(0).getCampana();
            int mes  = datos.isEmpty() ? 0 : datos.get(0).getMes();
            int anio = datos.isEmpty() ? 0 : datos.get(0).getAnio();
 
            XSSFRow rT = sheet.createRow(0);
            XSSFCell cT = rT.createCell(0);
            cT.setCellValue("Reporte de Supervisores — " + campana + "  (" + mes + "/" + anio + ")");
            cT.setCellStyle(csTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
 
            // ── Fila headers ──────────────────────────────────────────────────
            String[] headers = {
                "Campaña", "Supervisor", "Meta Sup.", "Com. Máx. Sup. (S/)",
                "Agente", "Meta Agente", "Com. Máx. Agente (S/)",
                "Ventas Activas", "Monto Total (S/)", "Comisión Gen. (S/)", "% Alcance"
            };
            XSSFRow rH = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(csHeader);
            }
 
            // ── Filas de datos ────────────────────────────────────────────────
            int rowIdx = 2;
            for (ReporteSupervisorDTO d : datos) {
                XSSFRow row = sheet.createRow(rowIdx++);
                setStr(row, 0, d.getCampana(),    csDato);
                setStr(row, 1, d.getSupervisor(), csDato);
                setNum(row, 2,  d.getMetaSupervisor()        != null ? d.getMetaSupervisor().doubleValue()        : 0, csNum);
                setNum(row, 3,  d.getComisionMaxSupervisor() != null ? d.getComisionMaxSupervisor().doubleValue() : 0, csNum);
                setStr(row, 4, d.getAgente(),     csDato);
                setNum(row, 5,  d.getMetaAgente()            != null ? d.getMetaAgente().doubleValue()            : 0, csNum);
                setNum(row, 6,  d.getComisionMaxAgente()     != null ? d.getComisionMaxAgente().doubleValue()     : 0, csNum);
                setNum(row, 7,  d.getVentasActivas()         != null ? d.getVentasActivas().doubleValue()         : 0, csNum);
                setNum(row, 8,  d.getMontoTotal()            != null ? d.getMontoTotal().doubleValue()            : 0, csNum);
                setNum(row, 9,  d.getComisionGenerada()      != null ? d.getComisionGenerada().doubleValue()      : 0, csNum);
                setNum(row, 10, d.getPctAlcanceAgente()      != null ? d.getPctAlcanceAgente().doubleValue()      : 0, csNum);
            }
 
            wb.write(out);
            return out.toByteArray();
        }
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
    private void setStr(XSSFRow row, int col, String val, XSSFCellStyle cs) {
        XSSFCell c = row.createCell(col);
        c.setCellValue(val != null ? val : "");
        c.setCellStyle(cs);
    }
 
    private void setNum(XSSFRow row, int col, double val, XSSFCellStyle cs) {
        XSSFCell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(cs);
    }
}