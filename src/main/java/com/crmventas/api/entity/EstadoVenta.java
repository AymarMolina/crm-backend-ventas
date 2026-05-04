package com.crmventas.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estados_venta", schema = "crm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(name = "es_final", nullable = false)
    private Boolean esFinal = false;

    @Column(nullable = false)
    private Short orden;
}
