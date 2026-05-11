    package com.crmventas.api.controller;

    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.*;
    import org.springframework.http.*;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

    import com.crmventas.api.dto.request.CambioEstadoRequest;
    import com.crmventas.api.dto.request.VentaRequest;
    import com.crmventas.api.dto.response.AlertaVentaResponse;
    import com.crmventas.api.dto.response.EstadoConteoResponse;
import com.crmventas.api.dto.response.HistorialEstadoResponse;
import com.crmventas.api.dto.response.PageResponse;
    import com.crmventas.api.dto.response.ResumenAsesorResponse;
    import com.crmventas.api.dto.response.TendenciaDiaResponse;
    import com.crmventas.api.dto.response.VentaResponse;
    import com.crmventas.api.dto.response.VentasPorCampanaResponse;
    import com.crmventas.api.service.impl.VentaService;

    import com.crmventas.api.service.impl.DashboardService;

    import java.util.List;
    import java.util.UUID;

    @RestController
    @RequestMapping("/ventas")
    @RequiredArgsConstructor
    public class VentaController {

        private final VentaService ventaService;
        private final DashboardService  dashboardService;

        @GetMapping
        public ResponseEntity<PageResponse<VentaResponse>> listar(
            @RequestParam(name = "campanaId",    required = false) UUID campanaId,
            @RequestParam(name = "agenteId",     required = false) UUID agenteId,
            @RequestParam(name = "estadoCodigo", required = false) String estadoCodigo,
            @RequestParam(name = "tieneAlerta",  required = false) Boolean tieneAlerta,
            @RequestParam(name = "page",         defaultValue = "0")  int page,
            @RequestParam(name = "size",         defaultValue = "20") int size
        ) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaVenta"));
            return ResponseEntity.ok(
                ventaService.listar(campanaId, agenteId, estadoCodigo, tieneAlerta, pageable)
            );
        }
        @GetMapping("/{id}")
        public ResponseEntity<VentaResponse> obtener(
            @PathVariable("id") UUID id
        ) {
            return ResponseEntity.ok(ventaService.obtener(id));
        }

        @GetMapping("/cliente/{clienteId}")
        public ResponseEntity<PageResponse<VentaResponse>> porCliente(
            @PathVariable("clienteId") UUID clienteId,
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "20") int size
        ) {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(ventaService.porCliente(clienteId, pageable));
        }

        @PostMapping
        public ResponseEntity<VentaResponse> crear(@Valid @RequestBody VentaRequest req) {
            VentaResponse created = ventaService.crear(req);
            var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
            return ResponseEntity.created(location).body(created);
        }

        /**
         * PATCH /api/ventas/{id}/estado
         * Roles: GERENTE, BACK_OFFICE
         */
        @PatchMapping("/{id}/estado")
        public ResponseEntity<VentaResponse> cambiarEstado(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CambioEstadoRequest req
        ) {
            return ResponseEntity.ok(ventaService.cambiarEstado(id, req));
        }
        /**
         * PATCH /api/ventas/{id}/archivar-caida
         * Marca como vista una venta caída (quita la alerta)
         */
        @PatchMapping("/{id}/archivar-caida")
        public ResponseEntity<Void> archivarCaida(@PathVariable("id") UUID id) {
            ventaService.archivarCaida(id);
            return ResponseEntity.noContent().build();
        }
        /**
         * PATCH /api/ventas/{id}/cliente/{clienteId}
         * Vincula una venta existente a una ficha de cliente
         */
        @PatchMapping("/{id}/cliente/{clienteId}")
        public ResponseEntity<VentaResponse> vincularCliente(
            @PathVariable("id")         UUID id,
            @PathVariable("clienteId")  UUID clienteId
        ) {
            return ResponseEntity.ok(ventaService.actualizarCliente(id, clienteId));
        }

        /**
         * DELETE /api/ventas/{id}  →  soft delete
         * Roles: GERENTE, BACK_OFFICE
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(
            @PathVariable("id") UUID id
        ) {
            ventaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }

            /**
         * GET /api/ventas/resumen?periodo=15d
         * KPIs del asesor autenticado: ventas activas, objetivo, monto, comisión, alertas.
         * El agenteId se extrae del JWT — el asesor solo ve sus propios datos.
         */
        @GetMapping("/resumen")
        public ResponseEntity<ResumenAsesorResponse> resumen(
            @RequestParam(name = "periodo", defaultValue = "15d") String periodo
        ) {
            return ResponseEntity.ok(dashboardService.getResumen(periodo));
        }
    
        /**
         * GET /api/ventas/tendencia?periodo=15d
         * Monto total por día del asesor autenticado.
         */
        @GetMapping("/tendencia")
        public ResponseEntity<List<TendenciaDiaResponse>> tendencia(
            @RequestParam(name = "periodo", defaultValue = "15d") String periodo
        ) {
            return ResponseEntity.ok(dashboardService.getTendencia(periodo));
        }
    
        /**
         * GET /api/ventas/por-campana?periodo=15d
         * Conteo de ventas agrupado por campaña/línea del asesor autenticado.
         */
        @GetMapping("/por-campana")
        public ResponseEntity<List<VentasPorCampanaResponse>> porCampana(
            @RequestParam(name = "periodo", defaultValue = "15d") String periodo
        ) {
            return ResponseEntity.ok(dashboardService.getPorCampana(periodo));
        }
    
        /**
         * GET /api/ventas/por-estado?periodo=15d
         * Conteo de ventas agrupado por estado del asesor autenticado.
         */
        @GetMapping("/por-estado")
        public ResponseEntity<List<EstadoConteoResponse>> porEstado(
            @RequestParam(name = "periodo", defaultValue = "15d") String periodo
        ) {
            return ResponseEntity.ok(dashboardService.getPorEstado(periodo));
        }
    
        /**
         * GET /api/ventas/alertas
         * Ventas observadas (tieneAlerta=true) del asesor autenticado.
         * Sin filtro de período: muestra TODAS las alertas pendientes.
         */
        @GetMapping("/alertas")
        public ResponseEntity<List<AlertaVentaResponse>> alertas() {
            return ResponseEntity.ok(dashboardService.getAlertas());
        }

        @GetMapping("/{id}/historial")
        public ResponseEntity<List<HistorialEstadoResponse>> getHistorial(
            @PathVariable("id") UUID id
        ) {
            return ResponseEntity.ok(ventaService.getHistorial(id));
        }
    }
