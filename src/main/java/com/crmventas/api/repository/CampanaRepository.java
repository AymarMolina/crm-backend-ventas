package com.crmventas.api.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Campana;

import java.util.UUID;

public interface CampanaRepository extends JpaRepository<Campana, UUID> {

    @Query("""
        SELECT c FROM Campana c JOIN FETCH c.linea l
        WHERE c.activo = true
          AND (:lineaCodigo IS NULL OR l.codigo = :lineaCodigo)
          AND (:mes IS NULL OR c.mes = :mes)
          AND (:anio IS NULL OR c.anio = :anio)
        """)
    Page<Campana> filtrar(
        @Param("lineaCodigo") String lineaCodigo,
        @Param("mes") Short mes,
        @Param("anio") Short anio,
        Pageable pageable
    );
}
