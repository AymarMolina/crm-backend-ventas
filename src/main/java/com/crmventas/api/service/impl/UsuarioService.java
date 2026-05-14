package com.crmventas.api.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.request.ActualizarUsuarioRequest;
import com.crmventas.api.dto.request.AsignarSupervisorRequest;
import com.crmventas.api.dto.request.UsuarioRequest;
import com.crmventas.api.dto.response.UsuarioResponse;
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

    public void designarSupervisor(AsignarSupervisorRequest dto) {
        int filasAfectadas = usuarioRepository.actualizarSupervisor(
            dto.agenteId(), 
            dto.supervisorId()
        );

        if (filasAfectadas == 0) {
            // Esto ocurre si el agente no es ROL 'AGENTE' o el supervisor no es ROL 'SUPERVISOR'
            throw new RuntimeException("No se pudo realizar la asignación. Verifique que los usuarios existan y tengan los roles correctos.");
        }
    }

    public List<UsuarioResponse> listarAsesores() {
        return usuarioRepository.findAllAgentes().stream()
            .filter(u -> u.getActivo())
            .map(u -> new UsuarioResponse(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getEmail(),
                // Evitamos NullPointerException si no tiene supervisor
                u.getSupervisor() != null ? u.getSupervisor().getNombres() : "Sin asignar"
            ))
            .collect(Collectors.toList());
    }
    public List<UsuarioResponse> listarSupervisores() {
        return usuarioRepository.findAllSupervisores().stream()
            .filter(u -> u.getActivo())
            .map(u -> new UsuarioResponse(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getEmail(),
                // Evitamos NullPointerException si no tiene supervisor
                u.getSupervisor() != null ? u.getSupervisor().getNombres() : "Sin asignar"
            ))
            .collect(Collectors.toList());
    }

    public List<UsuarioResponse> listarMiEquipo(UUID supervisorId) {
        return usuarioRepository.findAgentesBySupervisor(supervisorId).stream()
            .filter(u -> u.getActivo())
            .map(u -> new UsuarioResponse(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getEmail(),
                u.getSupervisor() != null ? u.getSupervisor().getNombres() : "Sin asignar"
            ))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> listarTodos() {
        return usuarioRepository.findAll().stream()
            .filter(u -> u.getActivo())
            .map(u -> Map.<String, Object>of(
                "id",              u.getId(),
                "nombres",         u.getNombres(),
                "apellidos",       u.getApellidos(),
                "email",           u.getEmail(),
                "rolCodigo",       u.getRol().getCodigo(),
                "activo",          u.getActivo(),
                "nombreSupervisor", u.getSupervisor() != null
                                    ? u.getSupervisor().getNombres() + " " + u.getSupervisor().getApellidos()
                                    : "Sin asignar"
            ))
            .collect(Collectors.toList());
    }
    public Map<String, Object> actualizar(UUID id, ActualizarUsuarioRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));

        if (!usuario.getEmail().equals(req.getEmail()) &&
            usuarioRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Ya existe un usuario con ese email");
        }

        Rol rol = rolRepository.findByCodigo(req.getRolCodigo())
            .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + req.getRolCodigo()));

        usuario.setNombres(req.getNombres());
        usuario.setApellidos(req.getApellidos());
        usuario.setEmail(req.getEmail());
        usuario.setRol(rol);

        Usuario saved = usuarioRepository.save(usuario);

        return Map.of(
            "id",       saved.getId(),
            "nombres",  saved.getNombres(),
            "apellidos",saved.getApellidos(),
            "email",    saved.getEmail(),
            "rolCodigo",saved.getRol().getCodigo(),
            "activo",   saved.getActivo()
        );
    }

    public void eliminar(UUID id, UUID eliminadoPorId) {
    if (!usuarioRepository.existsById(id)) {
        throw new NotFoundException("Usuario no encontrado: " + id);
    }
    usuarioRepository.softDelete(id, OffsetDateTime.now(), eliminadoPorId);
    }


    public List<Map<String, Object>> listarEliminados() {
        return usuarioRepository.findAllInactivos().stream()
            .map(u -> Map.<String, Object>of(
                "id",          u.getId(),
                "nombres",     u.getNombres(),
                "apellidos",   u.getApellidos(),
                "email",       u.getEmail(),
                "rolCodigo",   u.getRol().getCodigo(),
                "eliminadoEn", u.getEliminadoEn().toString()
            ))
            .collect(Collectors.toList());
    }

    public void reactivar(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
        usuario.setActivo(true);
        usuario.setEliminadoEn(null);
        usuario.setEliminadoPor(null);
        usuarioRepository.save(usuario);
    }
}
