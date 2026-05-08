package com.crmventas.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.entity.LineaProducto;
import com.crmventas.api.repository.LineaRepository;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

@RestController
@RequestMapping("/lineas")
@RequiredArgsConstructor
public class LineaController {

    private final LineaRepository lineaRepository;

    // ── GET /api/lineas ──────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> listar() {
        List<LineaProducto> lineas = lineaRepository.findAll();
        return ResponseEntity.ok(
            lineas.stream()
                  .filter(l -> l.getActivo())
                  .map(this::toMap)
                  .toList()
        );
    }

    // ── GET /api/lineas/{id} ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        return lineaRepository.findById(id)
            .map(l -> ResponseEntity.ok(toMap(l)))
            .orElse(ResponseEntity.notFound().build());
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(LineaProducto l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",     l.getId());
        m.put("codigo", l.getCodigo());
        m.put("nombre", l.getNombre());
        m.put("activo", l.getActivo());
        return m;
    }
}