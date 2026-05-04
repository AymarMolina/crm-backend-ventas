package com.crmventas.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crmventas.api.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByCodigo(String codigo);
}