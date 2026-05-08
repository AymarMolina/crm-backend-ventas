package com.crmventas.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.crmventas.api.entity.LineaProducto;

public interface LineaRepository extends JpaRepository<LineaProducto, Integer> {
}