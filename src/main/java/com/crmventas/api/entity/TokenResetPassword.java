package com.crmventas.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens_reset_password", schema = "seguridad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenResetPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn;


    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
    }
}