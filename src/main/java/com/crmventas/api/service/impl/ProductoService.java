package com.crmventas.api.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.crmventas.api.dto.request.ProductoRequest;
import com.crmventas.api.dto.response.ProductoResponse;
import com.crmventas.api.entity.Campana;
import com.crmventas.api.entity.Producto;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.repository.ProductoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CampanaRepository campanaRepository;

    public List<ProductoResponse> listarPorCampana(UUID campanaId) {
        return productoRepository.findByCampanaIdAndActivoTrue(campanaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Campana campana = campanaRepository.findById(req.getCampanaId())
                .orElseThrow(() -> new NotFoundException("Campaña no encontrada: " + req.getCampanaId()));

        Producto producto = Producto.builder()
                .campana(campana)
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .precio(req.getPrecio())
                .activo(true)
                .build();

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest req) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));

        producto.setNombre(req.getNombre());
        producto.setDescripcion(req.getDescripcion());
        producto.setPrecio(req.getPrecio());

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void desactivar(UUID id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .activo(p.getActivo())
                .campanaId(p.getCampana().getId())
                .campanaNombre(p.getCampana().getNombre())
                .build();
    }
}