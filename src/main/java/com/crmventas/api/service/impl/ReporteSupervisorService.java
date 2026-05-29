package com.crmventas.api.service.impl;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.io.InputStream;
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

            // ── Insertar Logo (Más Alto) ──────────────────────────────────────
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    byte[] bytes = org.apache.poi.util.IOUtils.toByteArray(is);
                    int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

                    CreationHelper helper = wb.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();

                    // ¡IMAGEN MÁS ALTA! Ahora abarca desde la fila 0 a la 6
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(2); 
                    anchor.setRow2(6); // Antes era 3

                    // Pequeño margen interno para que no quede pegado a los bordes
                    anchor.setDx1(org.apache.poi.util.Units.toEMU(10)); 
                    anchor.setDy1(org.apache.poi.util.Units.toEMU(10));
                    anchor.setDx2(-org.apache.poi.util.Units.toEMU(10));
                    anchor.setDy2(-org.apache.poi.util.Units.toEMU(10));

                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    drawing.createPicture(anchor, pictureIdx);
                } else {
                    System.out.println("Advertencia: No se encontró logo.png");
                }
            } catch (Exception e) {
                System.err.println("Error al cargar el logo: " + e.getMessage());
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

            // ── Fila título (Desplazada a la fila 7) ──────────────────────────
            String campana = datos.isEmpty() ? "-" : datos.get(0).getCampana();
            int mes  = datos.isEmpty() ? 0 : datos.get(0).getMes();
            int anio = datos.isEmpty() ? 0 : datos.get(0).getAnio();

            // Índice 7 = Fila 8 de Excel (justo debajo del logo más alto)
            XSSFRow rT = sheet.createRow(7); 
            XSSFCell cT = rT.createCell(0);
            cT.setCellValue("Reporte de Supervisores — " + campana + "  (" + mes + "/" + anio + ")");
            cT.setCellStyle(csTitulo);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 10));

            // ── Fila headers (Desplazada a la fila 8) ─────────────────────────
            String[] headers = {
                "Campaña", "Supervisor", "Meta Sup.", "Com. Máx. Sup. (S/)",
                "Agente", "Meta Agente", "Com. Máx. Agente (S/)",
                "Ventas Activas", "Monto Total (S/)", "Comisión Gen. (S/)", "% Alcance"
            };
            XSSFRow rH = sheet.createRow(8); // Índice 8
            for (int i = 0; i < headers.length; i++) {
                XSSFCell c = rH.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(csHeader);
            }

            // ── Filas de datos (Empiezan en la fila 9) ────────────────────────
            int rowIdx = 9; // Índice 9
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