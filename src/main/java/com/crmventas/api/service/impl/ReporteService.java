package com.crmventas.api.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.crmventas.api.Reporte.ReporteExcelBuilder;
import com.crmventas.api.repository.ReporteRepository;

import jakarta.transaction.Transactional;

import com.crmventas.api.dto.ReporteAsesorDTO;
import com.crmventas.api.dto.ReporteFiltroDTO;

@Service
@Transactional
public class ReporteService {
 
    private final ReporteRepository repo;
    private final ReporteExcelBuilder builder;
 
    public ReporteService(ReporteRepository repo, ReporteExcelBuilder builder) {
        this.repo    = repo;
        this.builder = builder;
    }
 
    /**
     * Genera el Excel y devuelve los bytes.
     *
     * @param filtro       Filtros del reporte (campanaId, fechaDesde, fechaHasta)
     * @param campanaNombre Nombre legible de la campaña (para el encabezado)
     * @param generadoPor  Nombre/rol del usuario autenticado (del JWT)
     */
    public byte[] generarReporte(
            ReporteFiltroDTO filtro,
            String campanaNombre,
            String generadoPor) throws IOException {

        List<ReporteAsesorDTO> asesores = repo.obtenerReportePorCampana(
            filtro.getCampanaId(),
            filtro.getFechaDesde(),
            filtro.getFechaHasta(),
            filtro.getSupervisorId(),  // nuevo
            filtro.getAgenteId()       // nuevo
        );

        return builder.build(
            asesores,
            campanaNombre,
            filtro.getFechaDesde(),
            filtro.getFechaHasta(),
            generadoPor
        );
    }
}
 