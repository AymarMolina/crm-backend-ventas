// LoginAuditService.java
package com.crmventas.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.entity.Usuario;
import com.crmventas.api.repository.UsuarioRepository;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAuditService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // transacción PROPIA, no hace rollback aunque el padre falle
    public void registrarIntentoFallido(Usuario usuario, int maxIntentos, int lockoutMinutos) {
        short intentos = (short) (usuario.getIntentosFallidos() + 1);
        usuario.setIntentosFallidos(intentos);

        if (intentos >= maxIntentos) {
            usuario.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(lockoutMinutos));
            log.warn("Cuenta bloqueada por {} minutos: {}", lockoutMinutos, usuario.getEmail());
        }

        usuarioRepository.save(usuario);
        log.info("Intento fallido #{} para: {}", intentos, usuario.getEmail());
    }
}