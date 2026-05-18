package com.crmventas.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.crmventas.api.dto.response.CampanaSelectorResponse;
import com.crmventas.api.dto.response.DashboardGerenteResponse;
import com.crmventas.api.service.DashboardGerenteService;

import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/dashboard/gerente")
@RequiredArgsConstructor
public class DashboardGerenteController {
 
    private final DashboardGerenteService dashboardService;
 
    @GetMapping("/campanas")
    public ResponseEntity<List<CampanaSelectorResponse>> getCampanas() {
        return ResponseEntity.ok(dashboardService.getCampanasActivas());
    }
 
    /**
     * Dashboard completo de una campaña específica.
     * Se llama cada vez que el gerente cambia de campaña en el selector.
     *
     * @param campanaId UUID de la campaña seleccionada
     */
    @GetMapping("/{campanaId}")
    public ResponseEntity<DashboardGerenteResponse> getDashboard(
            @PathVariable("campanaId") UUID campanaId) {   // ← nombre explícito
        return ResponseEntity.ok(dashboardService.getDashboard(campanaId));
    }
}
