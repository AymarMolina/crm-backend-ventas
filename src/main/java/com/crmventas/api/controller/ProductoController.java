package com.crmventas.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.dto.request.ProductoRequest;
import com.crmventas.api.dto.response.ProductoResponse;
import com.crmventas.api.service.impl.ProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/campanas")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // GET /api/campanas/{campanaId}/productos  ← el frontend llama esto al elegir campaña
   @GetMapping("/{campanaId}/productos")
    public ResponseEntity<List<ProductoResponse>> listar(
    @PathVariable("campanaId") UUID campanaId) {
        return ResponseEntity.ok(productoService.listarPorCampana(campanaId));
    }

    @PostMapping("/{campanaId}/productos")
    public ResponseEntity<ProductoResponse> crear(
            @PathVariable("campanaId") UUID campanaId,  // 👈
            @Valid @RequestBody ProductoRequest req) {
        req.setCampanaId(campanaId);
        return ResponseEntity.ok(productoService.crear(req));
    }

    @PutMapping("/{campanaId}/productos/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable("campanaId") UUID campanaId,  // 👈
            @PathVariable("id") UUID id,                // 👈
            @Valid @RequestBody ProductoRequest req) {
        req.setCampanaId(campanaId);
        return ResponseEntity.ok(productoService.actualizar(id, req));
    }

    @DeleteMapping("/{campanaId}/productos/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable("campanaId") UUID campanaId,  // 👈
            @PathVariable("id") UUID id) {              // 👈
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}