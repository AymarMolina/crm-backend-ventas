package com.crmventas.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Cliente;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByTipoDocAndNroDoc(String tipoDoc, String nroDoc);

    boolean existsByTipoDocAndNroDoc(String tipoDoc, String nroDoc);

    // Con la configuración en application.yml, esto funcionará sin errores:
    @Query("""
        SELECT c FROM Cliente c
        WHERE c.activo = true
        AND (:q IS NULL OR 
            LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR
            LOWER(c.apellidoP) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR
            LOWER(c.apellidoM) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR
            c.nroDoc LIKE CONCAT('%', CAST(:q AS string), '%') OR
            c.telefono LIKE CONCAT('%', CAST(:q AS string), '%'))
        """)
    Page<Cliente> buscar(@Param("q") String q, Pageable pageable);
}
