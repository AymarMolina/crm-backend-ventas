package com.crmventas.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.entity.Campana;
import com.crmventas.api.entity.LineaProducto;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.repository.LineaRepository;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/campanas")
@RequiredArgsConstructor
public class CampanaController {

    private final CampanaRepository campanaRepository;
    private final LineaRepository   lineaRepository;

    @GetMapping
    public ResponseEntity<?> listar(
        @RequestParam(name = "lineaCodigo", required = false) String lineaCodigo,
        @RequestParam(name = "mes",         required = false) Short mes,
        @RequestParam(name = "anio",        required = false) Short anio,
        @RequestParam(name = "page", defaultValue = "0")  int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "anio", "mes"));
        var result = campanaRepository.filtrar(lineaCodigo, mes, anio, pageable);
        return ResponseEntity.ok(result.map(this::toMap));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable(name = "id") UUID id) {
        Campana c = campanaRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Campaña no encontrada: " + id));
        return ResponseEntity.ok(toMap(c));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CrearCampanaRequest req) {

        LineaProducto linea = lineaRepository.findById(req.getLineaId())
            .orElseThrow(() -> new NotFoundException("Línea no encontrada: " + req.getLineaId()));

        Campana campana = new Campana();
        campana.setNombre(req.getNombre().trim());
        campana.setLinea(linea);
        campana.setMes(req.getMes());
        campana.setAnio(req.getAnio());
        campana.setObjetivoTotal(req.getObjetivoTotal());
        campana.setActivo(true);

        Campana guardada = campanaRepository.save(campana);

        return ResponseEntity
            .created(URI.create("/api/campanas/" + guardada.getId()))
            .body(toMap(guardada));
    }

    @Data
    public static class CrearCampanaRequest {

        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotNull(message = "La línea es obligatoria")
        private Integer lineaId; // ✅ Cambiado de UUID a Integer

        @NotNull(message = "El mes es obligatorio")
        @Min(value = 1,  message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12")
        private Short mes;

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2000, message = "El año no es válido")
        private Short anio;

        @NotNull(message = "El objetivo total es obligatorio")
        @Positive(message = "El objetivo total debe ser mayor a 0")
        private Integer objetivoTotal;
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