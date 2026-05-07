package com.crmventas.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
