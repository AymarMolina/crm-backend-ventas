package com.crmventas.api.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.request.CrearObjetivoRequest;
import com.crmventas.api.dto.response.ObjetivoResponse;
import com.crmventas.api.entity.Objetivo;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.repository.ObjetivoRepository;
import com.crmventas.api.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObjetivoService {

    private final ObjetivoRepository objetivoRepository;
    private final CampanaRepository campanaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ObjetivoResponse crear(CrearObjetivoRequest req) {

        var campana = campanaRepository.findById(req.campanaId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Campaña no encontrada: " + req.campanaId()));

        var usuario = usuarioRepository.findById(req.usuarioId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Usuario no encontrado: " + req.usuarioId()));

        if (objetivoRepository.existsByCampanaIdAndUsuarioId(req.campanaId(), req.usuarioId())) {
            throw new DataIntegrityViolationException(
                "Ya existe un objetivo para este usuario en esta campaña");
        }

        var objetivo = Objetivo.builder()
            .campana(campana)
            .usuario(usuario)
            .objetivoVentas(req.objetivoVentas())
            .montoComision(req.montoComision())
            .build();

        var saved = objetivoRepository.save(objetivo);
        return toResponse(saved);
    }

    private ObjetivoResponse toResponse(Objetivo o) {
        return new ObjetivoResponse(
            o.getId(),
            o.getCampana().getId(),
            o.getCampana().getNombre(),
            o.getUsuario().getId(),
            o.getUsuario().getNombres() + " " + o.getUsuario().getApellidos(),
            o.getObjetivoVentas(),
            o.getMontoComision(),
            o.getCreadoEn()
        );
    }

    public Optional<ObjetivoResponse> buscarPorUsuarioYCampana(UUID usuarioId, UUID campanaId) {
        return objetivoRepository.findByCampanaIdAndUsuarioId(campanaId, usuarioId)
            .map(this::toResponse);
    }

    @Transactional
    public ObjetivoResponse actualizar(Integer id, CrearObjetivoRequest req) {
        var objetivo = objetivoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Objetivo no encontrado: " + id));

        objetivo.setObjetivoVentas(req.objetivoVentas());
        objetivo.setMontoComision(req.montoComision());

        var saved = objetivoRepository.save(objetivo);
        return toResponse(saved);
    }

    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    // 2. Así debe quedar la función para el Asesor
    public List<ObjetivoResponse> listarObjetivosParaAsesorLogueado() {
        UUID agenteId = getUsuarioAutenticado().getId();

        return objetivoRepository.findByUsuarioId(agenteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public List<ObjetivoResponse> listarObjetivosPorUsuario(UUID usuarioId) {
        return objetivoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)   
                .toList();
    }
 
}