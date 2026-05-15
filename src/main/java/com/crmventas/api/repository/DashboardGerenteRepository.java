package com.crmventas.api.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface DashboardGerenteRepository {
 
    // Este repositorio NO extiende JpaRepository porque no mapea una sola entidad.
    // Los métodos se implementan vía NamedParameterJdbcTemplate en el Service Layer.
    // Ver: DashboardGerenteServiceImpl
}
 