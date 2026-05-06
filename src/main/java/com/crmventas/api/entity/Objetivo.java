package com.crmventas.api.entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.OffsetDateTime;
 
@Entity
@Table(name = "objetivos", schema = "crm",
       uniqueConstraints = @UniqueConstraint(columnNames = {"campana_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objetivo {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campana_id", nullable = false)
    private Campana campana;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
 
    @Column(name = "objetivo_ventas", nullable = false)
    private Integer objetivoVentas;
 
    @Column(name = "monto_comision", precision = 10, scale = 2)
    private BigDecimal montoComision;
 
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;
 
    @PrePersist
    void prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
    }
}