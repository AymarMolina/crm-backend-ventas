package com.crmventas.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.dto.request.LoginRequest;
import com.crmventas.api.dto.response.AuthResponse;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.exception.AccountLockedException;
import com.crmventas.api.exception.BusinessException;
import com.crmventas.api.repository.UsuarioRepository;
import com.crmventas.api.security.jwt.JwtService;

import java.time.OffsetDateTime;

import com.crmventas.api.dto.request.ForgotPasswordRequest;
import com.crmventas.api.dto.request.ResetPasswordRequest;
import com.crmventas.api.entity.TokenResetPassword;
import com.crmventas.api.repository.TokenResetPasswordRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenResetPasswordRepository tokenResetRepo;

    @Value("${app.security.max-intentos-login:5}")
    private int maxIntentos;

    @Value("${app.security.lockout-minutos:15}")
    private int lockoutMinutos;

    @Transactional
    public AuthResponse login(LoginRequest req) {
        log.info("Intentando login para: {}", req.getEmail());
    
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(req.getEmail())
            .orElseThrow(() -> {
                log.error("Usuario no encontrado o inactivo: {}", req.getEmail());
                return new BusinessException("Credenciales inválidas");
            });

        log.info("Usuario encontrado: {}, activo: {}", usuario.getEmail(), usuario.getActivo());
        log.info("Hash en BD: {}", usuario.getHashPassword());
        log.info("Password match: {}", passwordEncoder.matches(req.getPassword(), usuario.getHashPassword()));

        // Verificar bloqueo
        if (usuario.getBloqueadoHasta() != null &&
            usuario.getBloqueadoHasta().isAfter(OffsetDateTime.now())) {
            throw new AccountLockedException(
                "Cuenta bloqueada hasta " + usuario.getBloqueadoHasta()
            );
        }

        // Verificar password
        if (!passwordEncoder.matches(req.getPassword(), usuario.getHashPassword())) {
            registrarIntentoFallido(usuario);
            throw new BusinessException("Credenciales inválidas");
        }

        // Login exitoso: resetear intentos
        usuario.setIntentosFallidos((short) 0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoAcceso(OffsetDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(
            usuario.getId().toString(),
            usuario.getEmail(),
            usuario.getRol().getCodigo(),
            usuario.getDebeCambiarPass()
        );

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationMs() / 1000)
            .rol(usuario.getRol().getCodigo())
            .nombres(usuario.getNombres())
            .debeCambiarPass(usuario.getDebeCambiarPass())
            .build();
    }

    private void registrarIntentoFallido(Usuario usuario) {
        short intentos = (short) (usuario.getIntentosFallidos() + 1);
        usuario.setIntentosFallidos(intentos);
        if (intentos >= maxIntentos) {
            usuario.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(lockoutMinutos));
            log.warn("Cuenta bloqueada: {}", usuario.getEmail());
        }
        usuarioRepository.save(usuario);
    }
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req, HttpServletRequest httpReq) {
        // Siempre responde OK aunque el email no exista (evita user enumeration)
        usuarioRepository.findByEmailAndActivoTrue(req.getEmail()).ifPresent(usuario -> {

            // Invalidar tokens anteriores del mismo usuario
            tokenResetRepo.invalidarTokensAnteriores(usuario.getId(), OffsetDateTime.now());


            // Generar token seguro y hashearlo
            String rawToken  = generarTokenAleatorio();
            String tokenHash = sha256Hex(rawToken);

            TokenResetPassword tokenEntity = TokenResetPassword.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .expiraEn(OffsetDateTime.now().plusHours(1))
                .build();

            // TODO: enviar email con el link
            // El link que va en el correo:
            // https://tu-frontend.com/reset-password?token=<rawToken>
            tokenResetRepo.save(tokenEntity);
            emailService.enviarResetPassword(
                usuario.getEmail(),
                usuario.getNombres(),
                rawToken              // token RAW, nunca el hash
            );
        });
    }

    // ── método 2: confirmar nueva contraseña ────────────────────
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String tokenHash = sha256Hex(req.getToken());

        TokenResetPassword tokenEntity = tokenResetRepo
            .findByTokenHashAndUsadoEnIsNull(tokenHash)
            .orElseThrow(() -> new BusinessException("Token inválido o ya utilizado"));

        if (tokenEntity.getExpiraEn().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("El enlace de recuperación ha expirado");
        }

        Usuario usuario = tokenEntity.getUsuario();

        // Actualizar contraseña
        usuario.setHashPassword(passwordEncoder.encode(req.getNewPassword()));
        usuario.setDebeCambiarPass(false);
        usuario.setIntentosFallidos((short) 0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        // Marcar token como usado (uso único)
        tokenEntity.setUsadoEn(OffsetDateTime.now());
        tokenResetRepo.save(tokenEntity);

        log.info("Contraseña actualizada para usuario: {}", usuario.getEmail());
    }

    // ── utilidades privadas ─────────────────────────────────────
    private String generarTokenAleatorio() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes); // 64 chars hex
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generando hash SHA-256", e);
        }
    }
}
