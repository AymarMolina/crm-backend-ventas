package com.crmventas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.crmventas.api.dto.request.CambioEstadoRequest;
import com.crmventas.api.dto.request.VentaRequest;
import com.crmventas.api.dto.response.PageResponse;
import com.crmventas.api.dto.response.VentaResponse;
import com.crmventas.api.service.impl.VentaService;

import java.util.UUID;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<PageResponse<VentaResponse>> listar(
        @RequestParam(name = "campanaId",    required = false) UUID campanaId,
        @RequestParam(name = "agenteId",     required = false) UUID agenteId,
        @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo,
        @RequestParam(name = "tieneAlerta",  required = false) Boolean tieneAlerta,
        @RequestParam(name = "page",         defaultValue = "0")  int page,
        @RequestParam(name = "size",         defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaVenta"));
        return ResponseEntity.ok(
            ventaService.listar(campanaId, agenteId, estadoCodigo, tieneAlerta, pageable)
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtener(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(ventaService.obtener(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<PageResponse<VentaResponse>> porCliente(
        @PathVariable("clienteId") UUID clienteId,
        @RequestParam(name = "page", defaultValue = "0")  int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ventaService.porCliente(clienteId, pageable));
    }

    @PostMapping
    public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest req) {
        VentaResponse created = ventaService.crear(req);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * PATCH /api/ventas/{id}/estado
     * Roles: GERENTE, BACK_OFFICE
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VentaResponse> cambiarEstado(
        @PathVariable("id") UUID id,
        @Valid @RequestBody CambioEstadoRequest req
    ) {
        return ResponseEntity.ok(ventaService.cambiarEstado(id, req));
    }

    /**
     * PATCH /api/ventas/{id}/cliente/{clienteId}
     * Vincula una venta existente a una ficha de cliente
     */
    @PatchMapping("/{id}/cliente/{clienteId}")
    public ResponseEntity<VentaResponse> vincularCliente(
        @PathVariable("id")         UUID id,
        @PathVariable("clienteId")  UUID clienteId
    ) {
        return ResponseEntity.ok(ventaService.actualizarCliente(id, clienteId));
    }

    /**
     * DELETE /api/ventas/{id}  →  soft delete
     * Roles: GERENTE, BACK_OFFICE
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
        @PathVariable("id") UUID id
    ) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
