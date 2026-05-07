package com.crmventas.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Objetivo;

import java.util.Optional;
import java.util.UUID;
 

public interface ObjetivoRepository extends JpaRepository<Objetivo, Integer> {

    @Query("""
    SELECT o.objetivoVentas FROM Objetivo o
    WHERE o.usuario.id     = :agenteId
      AND o.campana.activo = true
    ORDER BY o.creadoEn DESC
    LIMIT 1
    """)
    Optional<Integer> findObjetivoActualPorAgente(
        @Param("agenteId") UUID agenteId
    );

    boolean existsByCampanaIdAndUsuarioId(UUID campanaId, UUID usuarioId);
}
 
