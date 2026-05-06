package com.crmventas.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crmventas.api.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    // Productos activos de una campaña (para el select del formulario)
    List<Producto> findByCampanaIdAndActivoTrue(UUID campanaId);

    // Todos los productos de una campaña (admin)
    List<Producto> findByCampanaId(UUID campanaId);
}