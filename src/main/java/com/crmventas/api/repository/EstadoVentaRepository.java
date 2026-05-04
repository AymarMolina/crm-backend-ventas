package com.crmventas.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crmventas.api.entity.EstadoVenta;

import java.util.Optional;

public interface EstadoVentaRepository extends JpaRepository<EstadoVenta, Integer> {
    Optional<EstadoVenta> findByCodigo(String codigo);
}
