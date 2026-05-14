package com.crmventas.api.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.crmventas.api.repository.UsuarioRepository;
import com.crmventas.api.security.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Si es una ruta de autenticación, saltar el filtro de inmediato
        String path = request.getServletPath();
        if (path.contains("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Obtener el header
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Validar Token
        String token = header.substring(7);
        try {
            if (jwtService.isValid(token)) {
                String userId = jwtService.getSubject(token);
                String rol = jwtService.getRol(token);

                usuarioRepository.findActivoById(UUID.fromString(userId)).ifPresent(u -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                        u, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        } catch (Exception e) {
            log.warn("Error procesando JWT en la ruta {}: {}", path, e.getMessage());
            // No bloqueamos aquí, dejamos que SecurityConfig decida si la ruta requiere auth
        }

        filterChain.doFilter(request, response);
    }
}
