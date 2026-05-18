package com.crmventas.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteAsesorDTO {
 
    private String asesorNombre;
    
    // Nuevos campos agregados basados en la base de datos
    private Integer objetivoVentas;
    private BigDecimal montoComision;
    
    private List<FilaProducto> productos;

    public ReporteAsesorDTO() {}
 
    // Constructor actualizado para incluir los nuevos campos
    public ReporteAsesorDTO(String asesorNombre, Integer objetivoVentas, BigDecimal montoComision, List<FilaProducto> productos) {
        this.asesorNombre = asesorNombre;
        this.objetivoVentas = objetivoVentas;
        this.montoComision = montoComision;
        this.productos = productos;
    }
 
    public String getAsesorNombre() { return asesorNombre; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }
 
    // Getters y Setters para Objetivos
    public Integer getObjetivoVentas() { return objetivoVentas; }
    public void setObjetivoVentas(Integer objetivoVentas) { this.objetivoVentas = objetivoVentas; }

    public BigDecimal getMontoComision() { return montoComision; }
    public void setMontoComision(BigDecimal montoComision) { this.montoComision = montoComision; }

    public List<FilaProducto> getProductos() { return productos; }
    public void setProductos(List<FilaProducto> productos) { this.productos = productos; }
    
    public static class FilaProducto {
        private String nombreProducto;
        private int cantidad;
        private BigDecimal precioUnitario;
 
        public FilaProducto() {}
 
        public FilaProducto(String nombreProducto, int cantidad, BigDecimal precioUnitario) {
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }
 
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
 
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
 
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    }
}