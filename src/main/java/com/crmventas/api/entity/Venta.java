package com.crmventas.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ventas", schema = "crm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id", nullable = false)
    private Campana campana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private Usuario agente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoVenta estado;

    @Column(name = "codigo_venta", nullable = false, unique = true, length = 40)
    private String codigoVenta;

    // Campos legacy (fallback cuando no hay ficha de cliente)
    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "cliente_doc", length = 20)
    private String clienteDoc;

    @Column(name = "cliente_telefono", length = 20)
    private String clienteTelefono;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    private String observaciones;

    @Column(name = "tiene_alerta", nullable = false)
    private Boolean tieneAlerta = false;

    @Column(name = "alerta_detalle")
    private String alertaDetalle;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Column(name = "eliminado_en")
    private OffsetDateTime eliminadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eliminado_por")
    private Usuario eliminadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
        actualizadoEn = OffsetDateTime.now();
        if (fechaVenta == null) fechaVenta = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
