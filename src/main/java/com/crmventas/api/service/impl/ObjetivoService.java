package com.crmventas.api.service.impl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.crmventas.api.dto.request.CrearObjetivoRequest;
import com.crmventas.api.dto.response.ObjetivoResponse;
import com.crmventas.api.entity.Objetivo;
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

        // Evita duplicado campana+usuario antes de persistir
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
}