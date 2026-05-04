package com.crmventas.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.dto.request.LoginRequest;
import com.crmventas.api.dto.response.AuthResponse;
import com.crmventas.api.repository.UsuarioRepository;
import com.crmventas.api.service.impl.AuthService;

import com.crmventas.api.dto.request.ForgotPasswordRequest;
import com.crmventas.api.dto.request.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req,
            HttpServletRequest httpReq) {
        authService.forgotPassword(req, httpReq);
        return ResponseEntity.ok(Map.of(
            "message", "Si el correo está registrado, recibirás un enlace en breve"
        ));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of(
            "message", "Contraseña actualizada correctamente"
        ));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean existe = usuarioRepository.existsByEmailAndActivoTrue(email);
        return ResponseEntity.ok(Map.of("existe", existe));
    }
}
