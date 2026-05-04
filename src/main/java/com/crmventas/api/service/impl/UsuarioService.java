package com.crmventas.api.service.impl;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.request.UsuarioRequest;
import com.crmventas.api.entity.Rol;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.exception.ConflictException;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.RolRepository;
import com.crmventas.api.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> crear(UsuarioRequest req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        Rol rol = rolRepository.findByCodigo(req.getRolCodigo())
            .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + req.getRolCodigo()));

        Usuario usuario = Usuario.builder()
            .nombres(req.getNombres())
            .apellidos(req.getApellidos())
            .email(req.getEmail())
            .hashPassword(passwordEncoder.encode(req.getPassword()))
            .rol(rol)
            .activo(true)
            .emailVerificado(true)
            .intentosFallidos((short) 0)
            .debeCambiarPass(false)
            .build();

        Usuario saved = usuarioRepository.save(usuario);

        return Map.of(
            "id",      saved.getId(),
            "email",   saved.getEmail(),
            "nombres", saved.getNombres(),
            "rol",     rol.getCodigo()
        );
    }
}
