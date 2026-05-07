package com.crmventas.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.dto.request.CrearObjetivoRequest;
import com.crmventas.api.dto.response.ObjetivoResponse;
import com.crmventas.api.service.impl.ObjetivoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/objetivos")
@RequiredArgsConstructor
public class ObjetivoController {

    private final ObjetivoService objetivoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'SUPERVISOR')")
    public ResponseEntity<ObjetivoResponse> crear(
            @Valid @RequestBody CrearObjetivoRequest request) {

        ObjetivoResponse response = objetivoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('GERENTE', 'SUPERVISOR')")
    public ResponseEntity<ObjetivoResponse> buscar(
            @RequestParam("usuarioId") UUID usuarioId,
            @RequestParam("campanaId") UUID campanaId) {

        return objetivoService.buscarPorUsuarioYCampana(usuarioId, campanaId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'SUPERVISOR')")
    public ResponseEntity<ObjetivoResponse> actualizar(
            @PathVariable("id") Integer id,          // 👈 nombre explícito
            @Valid @RequestBody CrearObjetivoRequest request) {

        ObjetivoResponse response = objetivoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-objetivos")
    @PreAuthorize("hasRole('AGENTE')")
    public ResponseEntity<List<ObjetivoResponse>> listarMisObjetivos() {
        return ResponseEntity.ok(objetivoService.listarObjetivosParaAsesorLogueado());
    }
}
