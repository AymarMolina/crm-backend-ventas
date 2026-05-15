package com.crmventas.api.service;

import java.util.List;
import java.util.UUID;

import com.crmventas.api.dto.response.CampanaSelectorResponse;
import com.crmventas.api.dto.response.DashboardGerenteResponse;

public interface DashboardGerenteService {
        /**
     * Lista todas las campañas activas para el selector del header.
     */
    List<CampanaSelectorResponse> getCampanasActivas();
 
    /**
     * Construye el dashboard completo para una campaña específica.
     *
     * @param campanaId UUID de la campaña seleccionada
     * @return DTO con todas las métricas, agentes y alertas de la campaña
     */
    DashboardGerenteResponse getDashboard(UUID campanaId);
}
