package com.crmventas.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crmventas.api.service.impl.UsuarioService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurgarUsuariosScheduler {

    private final UsuarioService usuarioService;

    // Todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void purgarUsuariosEliminados() {
        log.info("Iniciando purga de usuarios inactivos...");
        usuarioService.purgarEliminados();
    }
}