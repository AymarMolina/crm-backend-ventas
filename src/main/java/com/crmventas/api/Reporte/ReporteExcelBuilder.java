package com.crmventas.api.Reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.crmventas.api.dto.ReporteAsesorDTO;

/**
 * Genera el reporte de asesores en formato .xlsx incluyendo metas y comisiones.
 */
@Component
public class ReporteExcelBuilder {
 
    // ── Colores del ejemplo ────────────────────────────────────────────────────
    private static final String COLOR_TITULO      = "1F3864"; // azul marino
    private static final String COLOR_ASESOR      = "2E75B6"; // azul medio
    private static final String COLOR_HEADER_COL  = "BDD7EE"; // azul claro
    private static final String COLOR_FILA_PAR    = "DEEAF1"; // azul muy claro (filas pares)
    private static final String COLOR_BLANCO      = "FFFFFF";
    private static final String COLOR_NEGRO       = "000000";
 
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    /**
     * Genera el workbook y lo serializa a bytes listos para enviar como respuesta HTTP.
     */
    public byte[] build(
            List<ReporteAsesorDTO> asesores,
            String campana,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String generadoPor) throws IOException {
 
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
 
            XSSFSheet ws = wb.createSheet("Reporte");
 
            // ── Estilos reutilizables (Último parámetro define si lleva bordes THIN) ─────────────────
            CellStyle stTitulo      = crearEstilo(wb, COLOR_TITULO,     COLOR_BLANCO, true,  14, HorizontalAlignment.CENTER, false);
            CellStyle stAsesor      = crearEstilo(wb, COLOR_ASESOR,     COLOR_BLANCO, true,  11, HorizontalAlignment.CENTER, true);
            CellStyle stHeaderCol  = crearEstilo(wb, COLOR_HEADER_COL, COLOR_NEGRO,  true,  10, HorizontalAlignment.CENTER, true);
            CellStyle stDato       = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            CellStyle stDatoPar    = crearEstilo(wb, COLOR_FILA_PAR,   COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            CellStyle stMeta       = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.LEFT,   false);
            CellStyle stMetaLabel  = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  true,  10, HorizontalAlignment.LEFT,   false);
            
            // Estilos específicos con borde para la fila de objetivos de los asesores
            CellStyle stObjLabel   = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  true,  10, HorizontalAlignment.LEFT,   true);
            CellStyle stObjValue   = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
 
            // Estilos de moneda con bordes integrados
            CellStyle stMoneda     = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            stMoneda.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
 
            CellStyle stMonedaPar  = crearEstilo(wb, COLOR_FILA_PAR,   COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            stMonedaPar.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
 
            // ── Anchos de columna ─────────────────────────────────────────────
            ws.setColumnWidth(0, 30 * 256);  // A: Producto
            ws.setColumnWidth(1, 14 * 256);  // B: Cantidad / Objetivo
            ws.setColumnWidth(2, 22 * 256);  // C: Precio x producto / Etiqueta Comisión
            ws.setColumnWidth(3, 18 * 256);  // D: Venta final / Monto Comisión
 
            // ── FILA 1-2: Título ──────────────────────────────────────────────
            XSSFRow r1 = ws.createRow(0);
            r1.setHeightInPoints(22);
            XSSFCell cTitulo = r1.createCell(0);
            cTitulo.setCellValue("REPORTE DE VENTAS – CRM");
            cTitulo.setCellStyle(stTitulo);
            ws.addMergedRegion(new CellRangeAddress(0, 1, 0, 3)); // A1:D2
            ws.createRow(1).setHeightInPoints(8);
 
            // ── FILA 3: Metadatos ─────────────────────────────────────────────
            Row rMeta = ws.createRow(2);
            rMeta.setHeightInPoints(16);
 
            setCell(rMeta, 0, "Fecha generación:", stMetaLabel);
            setCell(rMeta, 1, java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), stMeta);
            setCell(rMeta, 2, "Período:", stMetaLabel);
 
            String periodo = fechaDesde.format(FMT_FECHA) + " – " + fechaHasta.format(FMT_FECHA);
            setCell(rMeta, 3, periodo, stMeta);
 
            // Fila 4 en blanco
            ws.createRow(3).setHeightInPoints(6);
 
            // Segunda línea de meta (fila 5)
            Row rMeta2 = ws.createRow(4);
            rMeta2.setHeightInPoints(16);
            setCell(rMeta2, 0, "Campaña:", stMetaLabel);
            setCell(rMeta2, 1, campana, stMeta);
            setCell(rMeta2, 2, "Generado por:", stMetaLabel);
            setCell(rMeta2, 3, generadoPor, stMeta);
 
            // Fila 6 en blanco (separador)
            ws.createRow(5).setHeightInPoints(6);
 
            // ── BLOQUES POR ASESOR ────────────────────────────────────────────
            int rowIdx = 6;  // empezamos desde la fila 7 (índice 6)
 
            for (int a = 0; a < asesores.size(); a++) {
                ReporteAsesorDTO asesor = asesores.get(a);
 
                // 1. Cabecera del asesor (Se crean todas las celdas del merge para mantener consistencia de color/borde)
                Row rAsesor = ws.createRow(rowIdx++);
                rAsesor.setHeightInPoints(18);
                for (int i = 0; i <= 3; i++) {
                    rAsesor.createCell(i).setCellStyle(stAsesor);
                }
                rAsesor.getCell(0).setCellValue("REPORTE " + asesor.getAsesorNombre().toUpperCase());
                ws.addMergedRegion(new CellRangeAddress(rAsesor.getRowNum(), rAsesor.getRowNum(), 0, 3));
 
                // 2. NUEVO: Fila de Objetivos y Comisión del Asesor
                Row rObjetivo = ws.createRow(rowIdx++);
                rObjetivo.setHeightInPoints(16);
                
                setCell(rObjetivo, 0, "Objetivo Asignado:", stObjLabel);
                setCell(rObjetivo, 1, asesor.getObjetivoVentas() != null ? asesor.getObjetivoVentas() : 0, stObjValue);
                setCell(rObjetivo, 2, "Comisión Estimada:", stObjLabel);
                
                double comision = asesor.getMontoComision() != null ? asesor.getMontoComision().doubleValue() : 0.0;
                setCell(rObjetivo, 3, comision, stMoneda);
                
                // 3. Cabecera de columnas de productos
                Row rHeaders = ws.createRow(rowIdx++);
                rHeaders.setHeightInPoints(16);
                setCell(rHeaders, 0, "Producto",          stHeaderCol);
                setCell(rHeaders, 1, "Cantidad",          stHeaderCol);
                setCell(rHeaders, 2, "Precio x producto", stHeaderCol);
                setCell(rHeaders, 3, "Venta final",        stHeaderCol);
 
                // 4. Filas de datos (Productos)
                List<ReporteAsesorDTO.FilaProducto> filas = asesor.getProductos();
 
                for (int i = 0; i < filas.size(); i++) {
                    ReporteAsesorDTO.FilaProducto fp = filas.get(i);
                    boolean par = (i % 2 == 1);
 
                    Row rDato = ws.createRow(rowIdx++);
                    rDato.setHeightInPoints(15);
 
                    setCell(rDato, 0, fp.getNombreProducto(), par ? stDatoPar : stDato);
                    setCell(rDato, 1, fp.getCantidad(),        par ? stDatoPar : stDato);
                    setCell(rDato, 2, fp.getPrecioUnitario().doubleValue(), par ? stDatoPar : stDato);
 
                    // Venta final = Cantidad × Precio
                    BigDecimal ventaFinal = fp.getPrecioUnitario().multiply(BigDecimal.valueOf(fp.getCantidad()));
 
                    Cell cVenta = rDato.createCell(3);
                    cVenta.setCellValue(ventaFinal.doubleValue());
                    cVenta.setCellStyle(par ? stMonedaPar : stMoneda);
                }
 
                // Fila en blanco separadora entre asesores
                ws.createRow(rowIdx++).setHeightInPoints(8);
            }
 
            wb.write(out);
            return out.toByteArray();
        }
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
 
    private CellStyle crearEstilo(XSSFWorkbook wb, String bgHex, String fgHex,
                                  boolean bold, int fontSize, HorizontalAlignment align, boolean conBordes) {
        XSSFCellStyle style = wb.createCellStyle();
 
        if (!bgHex.equals(COLOR_BLANCO)) {
            XSSFColor bg = new XSSFColor(hexToBytes(bgHex), new DefaultIndexedColorMap());
            style.setFillForegroundColor(bg);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
 
        XSSFFont font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        if (!fgHex.equals(COLOR_NEGRO)) {
            font.setColor(new XSSFColor(hexToBytes(fgHex), new DefaultIndexedColorMap()));
        }
        style.setFont(font);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
 
        // Inyectamos los bordes directamente al inicializar el estilo
        if (conBordes) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
 
        return style;
    }
 
    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }
 
    private void setCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }
 
    private void setCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }
 
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}