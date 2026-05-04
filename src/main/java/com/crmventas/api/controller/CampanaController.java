package com.crmventas.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.entity.Campana;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.CampanaRepository;

import java.util.*;

@RestController
@RequestMapping("/campanas")
@RequiredArgsConstructor
public class CampanaController {

    private final CampanaRepository campanaRepository;

    /**
     * GET /api/campanas?lineaCodigo=MOVIL&mes=4&anio=2026
     */
    @GetMapping
    public ResponseEntity<?> listar(
        // Agregamos el nombre explícito entre comillas
        @RequestParam(name = "lineaCodigo", required = false) String lineaCodigo,
        @RequestParam(name = "mes", required = false) Short mes,
        @RequestParam(name = "anio", required = false) Short anio,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "anio", "mes"));
        var result = campanaRepository.filtrar(lineaCodigo, mes, anio, pageable);
        return ResponseEntity.ok(result.map(this::toMap));
    }

    /**
     * GET /api/campanas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable(name = "id") UUID id) { // <-- También aquí
        Campana c = campanaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Campaña no encontrada: " + id));
        return ResponseEntity.ok(toMap(c));
    }

    private Map<String, Object> toMap(Campana c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            c.getId());
        m.put("nombre",        c.getNombre());
        m.put("linea",         c.getLinea().getNombre());
        m.put("lineaCodigo",   c.getLinea().getCodigo());
        m.put("mes",           c.getMes());
        m.put("anio",          c.getAnio());
        m.put("objetivoTotal", c.getObjetivoTotal());
        m.put("activo",        c.getActivo());
        return m;
    }
}
