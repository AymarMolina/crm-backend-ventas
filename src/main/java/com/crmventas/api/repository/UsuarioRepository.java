package com.crmventas.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.crmventas.api.entity.Usuario;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Modifying
    @Query(value = """
        UPDATE crm.usuarios 
        SET supervisor_id = :supervisorId, 
            actualizado_en = CURRENT_TIMESTAMP
        WHERE id = :agenteId 
        AND rol_id = (SELECT id FROM crm.roles WHERE codigo = 'AGENTE')
        AND EXISTS (SELECT 1 FROM crm.usuarios WHERE id = :supervisorId AND rol_id = (SELECT id FROM crm.roles WHERE codigo = 'SUPERVISOR'))
        """, nativeQuery = true)
    int actualizarSupervisor(
        @Param("agenteId") UUID agenteId, 
        @Param("supervisorId") UUID supervisorId
    );

    // Listar todos los agentes (útil para el Gerente)
    @Query(value = """
        SELECT u.* FROM crm.usuarios u
        JOIN crm.roles r ON u.rol_id = r.id
        WHERE r.codigo = 'AGENTE'
        """, nativeQuery = true)
    List<Usuario> findAllAgentes();

     // Listar todos los agentes (útil para el Gerente)
    @Query(value = """
        SELECT u.* FROM crm.usuarios u
        JOIN crm.roles r ON u.rol_id = r.id
        WHERE r.codigo = 'SUPERVISOR'
        """, nativeQuery = true)
    List<Usuario> findAllSupervisores();

     // Listar todos los agentes (útil para el Gerente)
    @Query(value = """
        SELECT u.* FROM crm.usuarios u
        JOIN crm.roles r ON u.rol_id = r.id
        WHERE r.codigo = 'SUPERVISOR'
        """, nativeQuery = true)
    List<Usuario> findAllBackOffice();

    // Listar agentes de un supervisor específico
    List<Usuario> findBySupervisorId(UUID supervisorId);

    // Filtra los agentes que tienen asignado a este supervisor específico
    @Query(value = """
        SELECT u.* FROM crm.usuarios u
        JOIN crm.roles r ON u.rol_id = r.id
        WHERE r.codigo = 'AGENTE' AND u.supervisor_id = :supervisorId
        """, nativeQuery = true)
    List<Usuario> findAgentesBySupervisor(@Param("supervisorId") UUID supervisorId);

    @Modifying
    @Query("""
        UPDATE Usuario u SET
            u.intentosFallidos = u.intentosFallidos + 1,
            u.bloqueadoHasta = CASE 
                WHEN (u.intentosFallidos + 1) >= :maxIntentos 
                THEN :bloqueadoHasta 
                ELSE u.bloqueadoHasta 
            END
        WHERE u.id = :id
        """)
    void incrementarIntentoFallido(
        @Param("id") UUID id,
        @Param("maxIntentos") int maxIntentos,
        @Param("bloqueadoHasta") OffsetDateTime bloqueadoHasta
    );

    // Soft delete: marcar como inactivo
    @Modifying
    @Query("""
        UPDATE Usuario u SET
            u.activo = false,
            u.eliminadoEn = :ahora,
            u.eliminadoPor = :eliminadoPor
        WHERE u.id = :id
        """)
    void softDelete(
        @Param("id") UUID id,
        @Param("ahora") OffsetDateTime ahora,
        @Param("eliminadoPor") UUID eliminadoPor
    );

    // Hard delete: eliminar los que llevan más de 30 días inactivos

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.activo = false ORDER BY u.eliminadoEn DESC")
    List<Usuario> findAllInactivos();
}
