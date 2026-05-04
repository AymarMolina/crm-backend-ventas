package com.crmventas.api.repository;

import com.crmventas.api.entity.TokenResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenResetPasswordRepository extends JpaRepository<TokenResetPassword, UUID> {

    Optional<TokenResetPassword> findByTokenHashAndUsadoEnIsNull(String tokenHash);

    // Invalida tokens anteriores del mismo usuario antes de crear uno nuevo
    @Modifying
    @Query("UPDATE TokenResetPassword t SET t.usadoEn = :ahora WHERE t.usuario.id = :usuarioId AND t.usadoEn IS NULL")
    void invalidarTokensAnteriores(
        @Param("usuarioId") UUID usuarioId,
        @Param("ahora") java.time.OffsetDateTime ahora
    );
}
