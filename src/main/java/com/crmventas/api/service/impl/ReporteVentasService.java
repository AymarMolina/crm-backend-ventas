package com.crmventas.api.service.impl;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.FiltroReporteDTO;
import com.crmventas.api.dto.VentaReporteDTO;
import com.crmventas.api.repository.Ventareporterepository;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
public class ReporteVentasService {
 
    private final Ventareporterepository repo;
 
    // ─────────────────────────────────────────────────────────────────────────
    // PUNTO DE ENTRADA SEGÚN ROL
    // ─────────────────────────────────────────────────────────────────────────
 
    public byte[] generarReporteAsesor(UUID agenteId, FiltroReporteDTO filtro) throws IOException {
        filtro.setAgenteId(agenteId);
        List<VentaReporteDTO> ventas = repo.buscarVentasFiltradas(filtro);
        return buildExcel(ventas, filtro, "ASESOR");
    }
 
    public byte[] generarReporteSupervisor(UUID supervisorId, FiltroReporteDTO filtro) throws IOException {
        List<UUID> idsEquipo = new ArrayList<>(repo.obtenerAsesorDesSupervisor(supervisorId));
        idsEquipo.add(supervisorId);
        filtro.setAgenteIds(idsEquipo);
        List<VentaReporteDTO> ventas = repo.buscarVentasFiltradas(filtro);
        return buildExcel(ventas, filtro, "SUPERVISOR");
    }
 
    public byte[] generarReporteGerente(FiltroReporteDTO filtro) throws IOException {
        if (filtro.getSupervisorId() != null) {
            List<UUID> idsEquipo = new ArrayList<>(repo.obtenerAsesorDesSupervisor(filtro.getSupervisorId()));
            idsEquipo.add(filtro.getSupervisorId());
            filtro.setAgenteIds(idsEquipo);
        }
        List<VentaReporteDTO> ventas = repo.buscarVentasFiltradas(filtro);
        return buildExcel(ventas, filtro, "GERENTE");
    }
 
    // ─────────────────────────────────────────────────────    ────────────────────
    // BUILDER PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────────
 
    private byte[] buildExcel(List<VentaReporteDTO> ventas,
                               FiltroReporteDTO filtro,
                               String rolGenerador) throws IOException {
 
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
 
            Estilos est = new Estilos(wb);
 
            buildSheetVentas(wb, est, ventas, filtro, rolGenerador);
            buildSheetResumenAsesor(wb, est, ventas);
            buildSheetResumenCampana(wb, est, ventas);
            if (!"ASESOR".equals(rolGenerador)) {
                buildSheetAlertas(wb, est, ventas);
            }
            buildSheetInstrucciones(wb, est);
 
            wb.write(out);
            return out.toByteArray();
        }
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // SHEET 1 : VENTAS DETALLE
    // ─────────────────────────────────────────────────────────────────────────
 
    private void buildSheetVentas(XSSFWorkbook wb, Estilos est,
                                   List<VentaReporteDTO> ventas,
                                   FiltroReporteDTO filtro,
                                   String rolGenerador) {
        XSSFSheet ws = wb.createSheet("Ventas");
        ws.createFreezePane(0, 5);
 
        // Título
        XSSFRow r1 = ws.createRow(0);
        r1.setHeightInPoints(30);
        XSSFCell t = r1.createCell(0);
        t.setCellValue("REPORTE DE VENTAS – CRM");
        t.setCellStyle(est.titulo);
        ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 13));
 
        // Fila vacía de separación (fila 1 ya cubierta por merge)
        ws.createRow(1).setHeightInPoints(4);
 
        // Metadata
        XSSFRow r3 = ws.createRow(2);
        r3.setHeightInPoints(18);
        celdaMeta(r3, 0, "Fecha generación:", est.metaLabel);
        celdaMeta(r3, 1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), est.metaValue);
        celdaMeta(r3, 3, "Período:", est.metaLabel);
        celdaMeta(r3, 4, formatFecha(filtro.getFechaDesde()) + " – " + formatFecha(filtro.getFechaHasta()), est.metaValue);
        celdaMeta(r3, 7, "Campaña:", est.metaLabel);
        celdaMeta(r3, 8, filtro.getCampanaNombre() != null ? filtro.getCampanaNombre() : "Todas", est.metaValue);
        celdaMeta(r3, 10, "Generado por:", est.metaLabel);
        celdaMeta(r3, 11, rolGenerador, est.metaValue);
 
        // Cabeceras
        String[] headers = {
            "Código Venta", "Fecha Venta", "Campaña", "Línea",
            "Asesor", "Supervisor", "Cliente", "Doc. Cliente",
            "Teléfono", "Monto (S/.)", "% Comisión", "Comisión (S/.)",
            "Estado", "Alerta"
        };
        XSSFRow r5 = ws.createRow(4);
        r5.setHeightInPoints(25);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell c = r5.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(est.cabecera);
        }
 
        // Datos
        int rowNum = 5;
        for (VentaReporteDTO v : ventas) {
            XSSFRow r = ws.createRow(rowNum++);
            r.setHeightInPoints(16);
            celda(r, 0,  v.getCodigoVenta(), est.dato);
            celda(r, 1,  v.getFechaVenta() != null
                    ? v.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", est.dato);
            celda(r, 2,  v.getCampanaNombre(), est.dato);
            celda(r, 3,  v.getLineaNombre(), est.dato);
            celda(r, 4,  v.getAgenteNombre(), est.dato);
            celda(r, 5,  v.getSupervisorNombre() != null ? v.getSupervisorNombre() : "—", est.dato);
            celda(r, 6,  v.getClienteNombre(), est.dato);
            celda(r, 7,  v.getClienteDoc(), est.dato);
            celda(r, 8,  v.getClienteTelefono(), est.dato);
            celdaNum(r, 9,  toBD(v.getMonto()), est.moneda);
            celdaNum(r, 10, toBD(v.getComisionPorcentaje()), est.porcentaje);
            celdaNum(r, 11, toBD(v.getComisionGenerada()), est.moneda);
 
            XSSFCell cEst = r.createCell(12);
            cEst.setCellValue(v.getEstadoCodigo());
            cEst.setCellStyle(est.estadoEstilo(v.getEstadoCodigo()));
 
            XSSFCell cAl = r.createCell(13);
            if (Boolean.TRUE.equals(v.getTieneAlerta())) {
                cAl.setCellValue("⚠ Alerta");
                cAl.setCellStyle(est.alerta);
            } else {
                cAl.setCellValue("");
                cAl.setCellStyle(est.dato);
            }
        }
 
        // Fila totales
        XSSFRow rTotal = ws.createRow(rowNum);
        rTotal.setHeightInPoints(18);
        XSSFCell cLbl = rTotal.createCell(0);
        cLbl.setCellValue("TOTALES");
        cLbl.setCellStyle(est.totalLabel);
        ws.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 8));
 
        // rowNum es 0-indexed; en Excel las filas son 1-indexed → rowNum+1
        int excelDataStart = 6;          // fila Excel donde arrancan los datos
        int excelDataEnd   = rowNum;     // fila Excel de la fila total (datos llegan hasta rowNum-1+1)
 
        XSSFCell cTMonto = rTotal.createCell(9);
        cTMonto.setCellFormula("SUM(J" + excelDataStart + ":J" + excelDataEnd + ")");
        cTMonto.setCellStyle(est.totalMoneda);
 
        rTotal.createCell(10).setCellStyle(est.totalLabel);
 
        XSSFCell cTCom = rTotal.createCell(11);
        cTCom.setCellFormula("SUM(L" + excelDataStart + ":L" + excelDataEnd + ")");
        cTCom.setCellStyle(est.totalMoneda);
 
        for (int c : new int[]{12, 13}) rTotal.createCell(c).setCellStyle(est.totalLabel);
 
        // Anchos
        int[] ws1 = {18, 12, 26, 14, 18, 18, 20, 14, 14, 14, 12, 14, 12, 10};
        for (int i = 0; i < ws1.length; i++) ws.setColumnWidth(i, ws1[i] * 256);
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // SHEET 2 : RESUMEN POR ASESOR
    // ─────────────────────────────────────────────────────────────────────────
 
    private void buildSheetResumenAsesor(XSSFWorkbook wb, Estilos est, List<VentaReporteDTO> ventas) {
        XSSFSheet ws = wb.createSheet("Resumen por Asesor");
        ws.createFreezePane(0, 3);
 
        XSSFRow r1 = ws.createRow(0);
        r1.setHeightInPoints(28);
        XSSFCell t = r1.createCell(0);
        t.setCellValue("RESUMEN POR ASESOR");
        t.setCellStyle(est.titulo);
        ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 7));
        ws.createRow(1).setHeightInPoints(4);
 
        String[] hdrs = {"Asesor", "Supervisor", "Total Ventas", "Activas",
                         "Observadas", "Caídas", "Monto Total (S/.)", "Comisión Total (S/.)"};
        XSSFRow rH = ws.createRow(2);
        rH.setHeightInPoints(22);
        for (int i = 0; i < hdrs.length; i++) {
            XSSFCell c = rH.createCell(i);
            c.setCellValue(hdrs[i]);
            c.setCellStyle(est.cabecera);
        }
 
        // Agrupar por asesor manteniendo orden de aparición
        Map<String, List<VentaReporteDTO>> porAsesor = ventas.stream()
                .collect(Collectors.groupingBy(VentaReporteDTO::getAgenteNombre,
                         LinkedHashMap::new, Collectors.toList()));
 
        int rowNum = 3;
        for (Map.Entry<String, List<VentaReporteDTO>> entry : porAsesor.entrySet()) {
            List<VentaReporteDTO> va = entry.getValue();
            XSSFRow r = ws.createRow(rowNum++);
            r.setHeightInPoints(16);
            celda(r, 0, entry.getKey(), est.dato);
            celda(r, 1, va.get(0).getSupervisorNombre() != null ? va.get(0).getSupervisorNombre() : "—", est.dato);
            celdaNum(r, 2, (double) va.size(), est.numero);
            celdaNum(r, 3, (double) va.stream().filter(v -> "ACTIVO".equals(v.getEstadoCodigo())).count(), est.numero);
            celdaNum(r, 4, (double) va.stream().filter(v -> "OBSERVADO".equals(v.getEstadoCodigo())).count(), est.numero);
            celdaNum(r, 5, (double) va.stream().filter(v -> "CAIDA".equals(v.getEstadoCodigo())).count(), est.numero);
            BigDecimal tm = va.stream().map(VentaReporteDTO::getMonto).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tc = va.stream().map(VentaReporteDTO::getComisionGenerada).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            celdaNum(r, 6, tm.doubleValue(), est.moneda);
            celdaNum(r, 7, tc.doubleValue(), est.moneda);
        }
 
        int dataEnd = rowNum; // fila Excel (1-indexed) de la última fila de datos
        XSSFRow rTotal = ws.createRow(rowNum);
        rTotal.setHeightInPoints(18);
        XSSFCell cLbl = rTotal.createCell(0);
        cLbl.setCellValue("TOTAL");
        cLbl.setCellStyle(est.totalLabel);
        ws.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
        rTotal.createCell(1).setCellStyle(est.totalLabel);
 
        // columnas C(2) D(3) E(4) F(5) G(6) H(7) → índices 2..7
        String[] cols = {"C","D","E","F","G","H"};
        for (int i = 0; i < cols.length; i++) {
            int colIdx = i + 2;
            XSSFCell ct = rTotal.createCell(colIdx);
            ct.setCellFormula("SUM(" + cols[i] + "4:" + cols[i] + dataEnd + ")");
            ct.setCellStyle(colIdx >= 6 ? est.totalMoneda : est.totalNumero);
        }
 
        int[] ws2 = {20, 20, 14, 12, 13, 11, 20, 20};
        for (int i = 0; i < ws2.length; i++) ws.setColumnWidth(i, ws2[i] * 256);
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // SHEET 3 : RESUMEN POR CAMPAÑA
    // ─────────────────────────────────────────────────────────────────────────
 
    private void buildSheetResumenCampana(XSSFWorkbook wb, Estilos est, List<VentaReporteDTO> ventas) {
        XSSFSheet ws = wb.createSheet("Resumen por Campaña");
        ws.createFreezePane(0, 3);
 
        XSSFRow r1 = ws.createRow(0);
        r1.setHeightInPoints(28);
        XSSFCell t = r1.createCell(0);
        t.setCellValue("RESUMEN POR CAMPAÑA");
        t.setCellStyle(est.titulo);
        ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 6));
        ws.createRow(1).setHeightInPoints(4);
 
        String[] hdrs = {"Campaña", "Línea", "Total Ventas", "Activas",
                         "% Activas", "Monto Total (S/.)", "Comisión Total (S/.)"};
        XSSFRow rH = ws.createRow(2);
        rH.setHeightInPoints(22);
        for (int i = 0; i < hdrs.length; i++) {
            XSSFCell c = rH.createCell(i);
            c.setCellValue(hdrs[i]);
            c.setCellStyle(est.cabecera);
        }
 
        Map<String, List<VentaReporteDTO>> porCampana = ventas.stream()
                .collect(Collectors.groupingBy(VentaReporteDTO::getCampanaNombre,
                         LinkedHashMap::new, Collectors.toList()));
 
        int rowNum = 3;
        for (Map.Entry<String, List<VentaReporteDTO>> entry : porCampana.entrySet()) {
            List<VentaReporteDTO> vc = entry.getValue();
            XSSFRow r = ws.createRow(rowNum++);
            r.setHeightInPoints(16);
            celda(r, 0, entry.getKey(), est.dato);
            celda(r, 1, vc.get(0).getLineaNombre(), est.dato);
            celdaNum(r, 2, (double) vc.size(), est.numero);
            long activas = vc.stream().filter(v -> "ACTIVO".equals(v.getEstadoCodigo())).count();
            celdaNum(r, 3, (double) activas, est.numero);
            // % activas: fórmula Excel usando la fila actual (rowNum ya fue incrementado → rowNum)
            XSSFCell cPct = r.createCell(4);
            cPct.setCellFormula("IF(C" + rowNum + "=0,0,D" + rowNum + "/C" + rowNum + ")");
            cPct.setCellStyle(est.porcentaje);
            BigDecimal tm = vc.stream().map(VentaReporteDTO::getMonto).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tc = vc.stream().map(VentaReporteDTO::getComisionGenerada).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            celdaNum(r, 5, tm.doubleValue(), est.moneda);
            celdaNum(r, 6, tc.doubleValue(), est.moneda);
        }
 
        int dataEnd = rowNum;
        XSSFRow rTotal = ws.createRow(rowNum);
        rTotal.setHeightInPoints(18);
        XSSFCell cLbl = rTotal.createCell(0);
        cLbl.setCellValue("TOTAL");
        cLbl.setCellStyle(est.totalLabel);
        ws.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
        rTotal.createCell(1).setCellStyle(est.totalLabel);
        rTotal.createCell(4).setCellStyle(est.totalLabel); // % no se suma
 
        for (int[] cd : new int[][]{{2,"C".charAt(0)},{3,"D".charAt(0)},{5,"F".charAt(0)},{6,"G".charAt(0)}}) {
            // cd[0]=colIdx, cd[1]=letraASCII
            XSSFCell ct = rTotal.createCell(cd[0]);
            ct.setCellFormula("SUM(" + (char)cd[1] + "4:" + (char)cd[1] + dataEnd + ")");
            ct.setCellStyle(cd[0] >= 5 ? est.totalMoneda : est.totalNumero);
        }
 
        int[] ws3 = {26, 14, 14, 12, 12, 20, 20};
        for (int i = 0; i < ws3.length; i++) ws.setColumnWidth(i, ws3[i] * 256);
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // SHEET 4 : ALERTAS
    // ─────────────────────────────────────────────────────────────────────────
 
    private void buildSheetAlertas(XSSFWorkbook wb, Estilos est, List<VentaReporteDTO> ventas) {
        XSSFSheet ws = wb.createSheet("Alertas");
        ws.createFreezePane(0, 3);
 
        XSSFRow r1 = ws.createRow(0);
        r1.setHeightInPoints(28);
        XSSFCell t = r1.createCell(0);
        t.setCellValue("⚠ VENTAS CON ALERTAS ACTIVAS");
        t.setCellStyle(est.tituloRojo);
        ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 7));
        ws.createRow(1).setHeightInPoints(4);
 
        String[] hdrs = {"Código Venta", "Campaña", "Asesor", "Supervisor",
                         "Cliente", "Estado", "Detalle Alerta", "Fecha"};
        XSSFRow rH = ws.createRow(2);
        rH.setHeightInPoints(22);
        for (int i = 0; i < hdrs.length; i++) {
            XSSFCell c = rH.createCell(i);
            c.setCellValue(hdrs[i]);
            c.setCellStyle(est.cabeceraRoja);
        }
 
        int rowNum = 3;
        for (VentaReporteDTO v : ventas.stream()
                .filter(x -> Boolean.TRUE.equals(x.getTieneAlerta()))
                .collect(Collectors.toList())) {
            XSSFRow r = ws.createRow(rowNum++);
            r.setHeightInPoints(16);
            celda(r, 0, v.getCodigoVenta(), est.datoAlerta);
            celda(r, 1, v.getCampanaNombre(), est.datoAlerta);
            celda(r, 2, v.getAgenteNombre(), est.datoAlerta);
            celda(r, 3, v.getSupervisorNombre() != null ? v.getSupervisorNombre() : "—", est.datoAlerta);
            celda(r, 4, v.getClienteNombre(), est.datoAlerta);
            XSSFCell cEst = r.createCell(5);
            cEst.setCellValue(v.getEstadoCodigo());
            cEst.setCellStyle(est.estadoEstilo(v.getEstadoCodigo()));
            celda(r, 6, v.getAlertaDetalle() != null ? v.getAlertaDetalle() : "—", est.datoAlerta);
            celda(r, 7, v.getFechaVenta() != null
                    ? v.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", est.datoAlerta);
        }
 
        int[] ws4 = {18, 24, 18, 18, 20, 12, 35, 12};
        for (int i = 0; i < ws4.length; i++) ws.setColumnWidth(i, ws4[i] * 256);
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // SHEET 5 : INSTRUCCIONES TABLA DINÁMICA
    // ─────────────────────────────────────────────────────────────────────────
 
    private void buildSheetInstrucciones(XSSFWorkbook wb, Estilos est) {
        XSSFSheet ws = wb.createSheet("📊 Tabla Dinámica");
 
        XSSFRow r1 = ws.createRow(0);
        r1.setHeightInPoints(28);
        XSSFCell t = r1.createCell(0);
        t.setCellValue("CÓMO CREAR TU TABLA DINÁMICA");
        t.setCellStyle(est.titulo);
        ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 5));
        ws.createRow(1).setHeightInPoints(4);
 
        String[] pasos = {
            "1.  Ve a la hoja 'Ventas'",
            "2.  Haz clic en cualquier celda dentro de la tabla de datos",
            "3.  Ve al menú: Insertar → Tabla dinámica",
            "4.  Selecciona 'Nueva hoja de cálculo' y haz clic en Aceptar",
            "5.  En el panel de campos, arrastra:",
            "        • FILAS:    Asesor  /  Campaña  /  Línea",
            "        • COLUMNAS: Estado  (o Mes si filtraste por fecha)",
            "        • VALORES:  Monto → Suma  |  Comisión → Suma  |  Código Venta → Recuento",
            "6.  Usa los filtros del panel para acotar por Supervisor, Campaña, Fecha, etc.",
        };
 
        int row = 3;
        for (String paso : pasos) {
            XSSFRow r = ws.createRow(row++);
            r.setHeightInPoints(18);
            XSSFCell c = r.createCell(0);
            c.setCellValue(paso);
            c.setCellStyle(est.instruccion);
            ws.addMergedRegion(new CellRangeAddress(row - 1, row - 1, 0, 5));
        }
        ws.setColumnWidth(0, 80 * 256);
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
 
    /** Celda de texto */
    private void celda(XSSFRow row, int col, String value, XSSFCellStyle style) {
        XSSFCell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }
 
    /** Celda numérica (Double) */
    private void celdaNum(XSSFRow row, int col, Double value, XSSFCellStyle style) {
        XSSFCell c = row.createCell(col);
        if (value != null) c.setCellValue(value);
        c.setCellStyle(style);
    }
 
    /** Celda numérica (BigDecimal → Double) */
    private void celdaNum(XSSFRow row, int col, BigDecimal value, XSSFCellStyle style) {
        celdaNum(row, col, value != null ? value.doubleValue() : null, style);
    }
 
    /** Celda de metadata (label / valor) */
    private void celdaMeta(XSSFRow row, int col, String value, XSSFCellStyle style) {
        XSSFCell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }
 
    private String formatFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
    }
 
    private double toBD(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }
 
    // ─────────────────────────────────────────────────────────────────────────
    // CLASE INTERNA: ESTILOS  (todo XSSFCellStyle para evitar type mismatch)
    // ─────────────────────────────────────────────────────────────────────────
 
    private static class Estilos {
 
        private final XSSFWorkbook wb;
 
        final XSSFCellStyle titulo;
        final XSSFCellStyle tituloRojo;
        final XSSFCellStyle cabecera;
        final XSSFCellStyle cabeceraRoja;
        final XSSFCellStyle dato;
        final XSSFCellStyle datoAlerta;
        final XSSFCellStyle metaLabel;
        final XSSFCellStyle metaValue;
        final XSSFCellStyle moneda;
        final XSSFCellStyle numero;
        final XSSFCellStyle porcentaje;
        final XSSFCellStyle totalLabel;
        final XSSFCellStyle totalMoneda;
        final XSSFCellStyle totalNumero;
        final XSSFCellStyle alerta;
        final XSSFCellStyle instruccion;
 
        private final Map<String, XSSFCellStyle> mapaEstados = new HashMap<>();
 
        Estilos(XSSFWorkbook wb) {
            this.wb = wb;
 
            titulo      = mkTitulo("1F3864", "FFFFFF", 16);
            tituloRojo  = mkTitulo("C00000", "FFFFFF", 14);
            cabecera    = mkCabecera("2E75B6", "FFFFFF");
            cabeceraRoja = mkCabecera("C00000", "FFFFFF");
            dato        = mkDato(null);
            datoAlerta  = mkDato("FFF2CC");
            metaLabel   = mkMeta(true);
            metaValue   = mkMeta(false);
            moneda      = mkNumero("#,##0.00");
            numero      = mkNumero("0");
            porcentaje  = mkNumero("0.00%");
            totalLabel  = mkTotal("1F3864", "FFFFFF", null);
            totalMoneda = mkTotal("1F3864", "FFFFFF", "#,##0.00");
            totalNumero = mkTotal("1F3864", "FFFFFF", "0");
            alerta      = mkAlerta();
            instruccion = mkInstruccion();
 
            mapaEstados.put("ACTIVO",     mkEstado("70AD47", "000000"));
            mapaEstados.put("EN_PROCESO", mkEstado("FFD966", "000000"));
            mapaEstados.put("OBSERVADO",  mkEstado("ED7D31", "FFFFFF"));
            mapaEstados.put("CAIDA",      mkEstado("FF0000", "FFFFFF"));
        }
 
        XSSFCellStyle estadoEstilo(String codigo) {
            return mapaEstados.getOrDefault(codigo, dato);
        }
 
        // ── Fábrica de estilos ──────────────────────────────────────────────
 
        private XSSFCellStyle mkTitulo(String bg, String fg, int size) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setFillForegroundColor(color(bg));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setAlignment(HorizontalAlignment.CENTER);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(true);
            f.setFontHeightInPoints((short) size);
            f.setColor(color(fg));
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkCabecera(String bg, String fg) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setFillForegroundColor(color(bg));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setAlignment(HorizontalAlignment.CENTER);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            s.setWrapText(true);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(true);
            f.setFontHeightInPoints((short) 9);
            f.setColor(color(fg));
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkDato(String bgHex) {
            XSSFCellStyle s = wb.createCellStyle();
            if (bgHex != null) {
                s.setFillForegroundColor(color(bgHex));
                s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setFontHeightInPoints((short) 9);
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkMeta(boolean bold) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(bold);
            f.setFontHeightInPoints((short) 9);
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkNumero(String format) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            s.setAlignment(HorizontalAlignment.RIGHT);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setFontHeightInPoints((short) 9);
            s.setFont(f);
            s.setDataFormat(wb.createDataFormat().getFormat(format));
            return s;
        }
 
        private XSSFCellStyle mkTotal(String bg, String fg, String format) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setFillForegroundColor(color(bg));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setAlignment(HorizontalAlignment.RIGHT);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(true);
            f.setFontHeightInPoints((short) 9);
            f.setColor(color(fg));
            s.setFont(f);
            if (format != null) s.setDataFormat(wb.createDataFormat().getFormat(format));
            return s;
        }
 
        private XSSFCellStyle mkEstado(String bg, String fg) {
            XSSFCellStyle s = wb.createCellStyle();
            s.setFillForegroundColor(color(bg));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setAlignment(HorizontalAlignment.CENTER);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(true);
            f.setFontHeightInPoints((short) 9);
            f.setColor(color(fg));
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkAlerta() {
            XSSFCellStyle s = wb.createCellStyle();
            s.setAlignment(HorizontalAlignment.CENTER);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            borde(s, "CCCCCC");
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setBold(true);
            f.setFontHeightInPoints((short) 9);
            f.setColor(color("FF0000"));
            s.setFont(f);
            return s;
        }
 
        private XSSFCellStyle mkInstruccion() {
            XSSFCellStyle s = wb.createCellStyle();
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont f = wb.createFont();
            f.setFontName("Arial"); f.setFontHeightInPoints((short) 10);
            s.setFont(f);
            return s;
        }
 
        // ── Utilidades ──────────────────────────────────────────────────────
 
        private void borde(XSSFCellStyle s, String hexColor) {
            XSSFColor c = color(hexColor);
            s.setBorderTop(BorderStyle.THIN);    s.setTopBorderColor(c);
            s.setBorderBottom(BorderStyle.THIN); s.setBottomBorderColor(c);
            s.setBorderLeft(BorderStyle.THIN);   s.setLeftBorderColor(c);
            s.setBorderRight(BorderStyle.THIN);  s.setRightBorderColor(c);
        }
 
        private XSSFColor color(String hex) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, null);
        }
    }
}