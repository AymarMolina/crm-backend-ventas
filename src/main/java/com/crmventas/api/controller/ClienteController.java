package com.crmventas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.crmventas.api.dto.request.ClienteRequest;
import com.crmventas.api.dto.response.ClienteResponse;
import com.crmventas.api.dto.response.PageResponse;
import com.crmventas.api.service.impl.ClienteService;

import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<PageResponse<ClienteResponse>> listar(
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "sort", defaultValue = "apellidoP") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(clienteService.listar(q, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
        @PathVariable("id") UUID id, // Added ("id")
        @Valid @RequestBody ClienteRequest req
    ) {
        return ResponseEntity.ok(clienteService.actualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable("id") UUID id) { // Added ("id")
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<ClienteResponse> buscarPorDoc(
        @RequestParam(value = "tipoDoc") String tipoDoc,
        @RequestParam(value = "nroDoc") String nroDoc
    ) {
        return ResponseEntity.ok(clienteService.buscarPorDoc(tipoDoc, nroDoc));
    }

    /**
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest req) {
        ClienteResponse created = clienteService.crear(req);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }
}
