package com.crmventas.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "campanas", schema = "crm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linea_id", nullable = false)
    private LineaProducto linea;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private Short mes;

    @Column(nullable = false)
    private Short anio;

    @Column(name = "objetivo_total", nullable = false)
    private Integer objetivoTotal = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    // ✅ Esto evita el error de campos NOT NULL vacíos
    @PrePersist
    void onCreate() {
        this.creadoEn     = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = OffsetDateTime.now();
    }
}