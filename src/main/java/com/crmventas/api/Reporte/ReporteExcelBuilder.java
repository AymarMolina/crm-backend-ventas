package com.crmventas.api.Reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
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

            // ── Anchos de columna ─────────────────────────────────────────────
            ws.setColumnWidth(0, 30 * 256);  // A: Producto
            ws.setColumnWidth(1, 14 * 256);  // B: Cantidad / Objetivo
            ws.setColumnWidth(2, 22 * 256);  // C: Precio x producto / Etiqueta Comisión
            ws.setColumnWidth(3, 18 * 256);  // D: Venta final / Monto Comisión
 
            // ── 0. INSERTAR LOGO (MÁS ALTO) ───────────────────────────────────
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

                    CreationHelper helper = wb.getCreationHelper();
                    Drawing<?> drawing = ws.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();

                    // ¡AQUÍ ES MÁS ALTO! Ocupa desde Columna 0 a 1 y Fila 0 a 5 (Seis filas en total)
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(2); 
                    anchor.setRow2(6); // Antes era 4

                    // Márgenes
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

            // ── Estilos reutilizables ─────────────────────────────────────────
            CellStyle stTitulo      = crearEstilo(wb, COLOR_TITULO,     COLOR_BLANCO, true,  14, HorizontalAlignment.CENTER, false);
            CellStyle stAsesor      = crearEstilo(wb, COLOR_ASESOR,     COLOR_BLANCO, true,  11, HorizontalAlignment.CENTER, true);
            CellStyle stHeaderCol  = crearEstilo(wb, COLOR_HEADER_COL, COLOR_NEGRO,  true,  10, HorizontalAlignment.CENTER, true);
            CellStyle stDato       = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            CellStyle stDatoPar    = crearEstilo(wb, COLOR_FILA_PAR,   COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            CellStyle stMeta       = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.LEFT,   false);
            CellStyle stMetaLabel  = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  true,  10, HorizontalAlignment.LEFT,   false);
            
            // Estilos específicos con borde para la fila de objetivos
            CellStyle stObjLabel   = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  true,  10, HorizontalAlignment.LEFT,   true);
            CellStyle stObjValue   = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
 
            // Estilos de moneda con bordes integrados
            CellStyle stMoneda     = crearEstilo(wb, COLOR_BLANCO,     COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            stMoneda.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
 
            CellStyle stMonedaPar  = crearEstilo(wb, COLOR_FILA_PAR,   COLOR_NEGRO,  false, 10, HorizontalAlignment.CENTER, true);
            stMonedaPar.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
 
            // ── FILA 7-8 (Índice 6-7): Título (Desplazado más abajo) ──────────
            XSSFRow r1 = ws.createRow(6);
            r1.setHeightInPoints(22);
            Cell cTitulo = r1.createCell(0);
            cTitulo.setCellValue("REPORTE DE VENTAS – CRM");
            cTitulo.setCellStyle(stTitulo);
            ws.addMergedRegion(new CellRangeAddress(6, 7, 0, 3)); // A7:D8
            ws.createRow(7).setHeightInPoints(8);
 
            // ── FILA 9 (Índice 8): Metadatos ──────────────────────────────────
            Row rMeta = ws.createRow(8);
            rMeta.setHeightInPoints(16);
 
            setCell(rMeta, 0, "Fecha generación:", stMetaLabel);
            setCell(rMeta, 1, java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), stMeta);
            setCell(rMeta, 2, "Período:", stMetaLabel);
 
            String periodo = fechaDesde.format(FMT_FECHA) + " – " + fechaHasta.format(FMT_FECHA);
            setCell(rMeta, 3, periodo, stMeta);
 
            // Fila 10 (Índice 9) en blanco
            ws.createRow(9).setHeightInPoints(6);
 
            // ── FILA 11 (Índice 10): Segunda línea de meta ────────────────────
            Row rMeta2 = ws.createRow(10);
            rMeta2.setHeightInPoints(16);
            setCell(rMeta2, 0, "Campaña:", stMetaLabel);
            setCell(rMeta2, 1, campana, stMeta);
            setCell(rMeta2, 2, "Generado por:", stMetaLabel);
            setCell(rMeta2, 3, generadoPor, stMeta);
 
            // Fila 12 (Índice 11) en blanco (separador)
            ws.createRow(11).setHeightInPoints(6);
 
            // ── BLOQUES POR ASESOR ────────────────────────────────────────────
            int rowIdx = 12;  // Empezamos los datos desde la fila 13 (índice 12)
 
            for (int a = 0; a < asesores.size(); a++) {
                ReporteAsesorDTO asesor = asesores.get(a);
 
                // 1. Cabecera del asesor 
                Row rAsesor = ws.createRow(rowIdx++);
                rAsesor.setHeightInPoints(18);
                for (int i = 0; i <= 3; i++) {
                    rAsesor.createCell(i).setCellStyle(stAsesor);
                }
                rAsesor.getCell(0).setCellValue("REPORTE " + asesor.getAsesorNombre().toUpperCase());
                ws.addMergedRegion(new CellRangeAddress(rAsesor.getRowNum(), rAsesor.getRowNum(), 0, 3));
 
                // 2. Fila de Objetivos y Comisión del Asesor
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