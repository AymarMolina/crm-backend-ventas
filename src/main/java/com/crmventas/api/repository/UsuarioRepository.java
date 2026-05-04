package com.crmventas.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);
    boolean existsByEmailAndActivoTrue(String email);
    @Query("""
        SELECT u FROM Usuario u
        JOIN FETCH u.rol
        WHERE u.id = :id AND u.activo = true
        """)
    Optional<Usuario> findActivoById(@Param("id") UUID id);
}
