package com.crmventas.api.controller;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crmventas.api.dto.request.ActualizarUsuarioRequest;
import com.crmventas.api.dto.request.AsignarSupervisorRequest;
import com.crmventas.api.dto.request.UsuarioRequest;
import com.crmventas.api.dto.response.UsuarioResponse;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.service.impl.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.ok(usuarioService.crear(req));
    }

    @PatchMapping("/designar-supervisor")
    public ResponseEntity<?> designarSupervisor(@Valid @RequestBody AsignarSupervisorRequest dto) {
        usuarioService.designarSupervisor(dto);
        
        return ResponseEntity.ok(Map.of(
            "message", "Jerarquía actualizada correctamente",
            "status", "success"
        ));
    }
    @GetMapping("/asesores")
    public ResponseEntity<List<UsuarioResponse>> listarAsesores() {
        return ResponseEntity.ok(usuarioService.listarAsesores());
    }
    @GetMapping("/supervisores")
    public ResponseEntity<List<UsuarioResponse>> listarSupervisores() {
        return ResponseEntity.ok(usuarioService.listarSupervisores());
    }

    @GetMapping("/mi-equipo")
    public ResponseEntity<List<UsuarioResponse>> listarMiEquipo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Usuario usuarioAutenticado) {
            return ResponseEntity.ok(usuarioService.listarMiEquipo(usuarioAutenticado.getId()));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable("id") UUID id,          // ← agregar "id"
            @Valid @RequestBody ActualizarUsuarioRequest req) {
        return ResponseEntity.ok(usuarioService.actualizar(id, req));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable("id") UUID id,
            Authentication authentication) {

        // Obtenemos quién está haciendo la acción para auditoria
        UUID eliminadoPorId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Usuario u) {
            eliminadoPorId = u.getId();
        }

        usuarioService.eliminar(id, eliminadoPorId);

        return ResponseEntity.ok(Map.of(
            "message", "Usuario desactivado correctamente",
            "status",  "success"
        ));
    }
    @GetMapping("/eliminados")
    public ResponseEntity<List<Map<String, Object>>> listarEliminados() {
        return ResponseEntity.ok(usuarioService.listarEliminados());
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Map<String, Object>> reactivar(@PathVariable("id") UUID id) {
        usuarioService.reactivar(id);
        return ResponseEntity.ok(Map.of("message", "Usuario reactivado correctamente"));
    }
}
