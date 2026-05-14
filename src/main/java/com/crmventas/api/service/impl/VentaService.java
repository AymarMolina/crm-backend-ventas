package com.crmventas.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.dto.request.CambioEstadoRequest;
import com.crmventas.api.dto.request.VentaRequest;
import com.crmventas.api.dto.response.AlertaVentaResponse;
import com.crmventas.api.dto.response.EstadoConteoResponse;
import com.crmventas.api.dto.response.HistorialEstadoResponse;
import com.crmventas.api.dto.response.PageResponse;
import com.crmventas.api.dto.response.VentaResponse;
import com.crmventas.api.entity.Campana;
import com.crmventas.api.entity.Cliente;
import com.crmventas.api.entity.EstadoVenta;
import com.crmventas.api.entity.Producto;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.entity.Venta;
import com.crmventas.api.exception.BusinessException;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.repository.ClienteRepository;
import com.crmventas.api.repository.EstadoVentaRepository;
import com.crmventas.api.repository.HistorialEstadoRepository;
import com.crmventas.api.repository.ProductoRepository;
import com.crmventas.api.repository.VentaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final CampanaRepository campanaRepository;
    private final EstadoVentaRepository estadoVentaRepository;
    private final ProductoRepository productoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    public PageResponse<VentaResponse> listar(UUID campanaId, UUID agenteId,
                                               String estadoCodigo, Boolean tieneAlerta,
                                               Pageable pageable) {
        return PageResponse.of(
            ventaRepository.filtrar(campanaId, agenteId, estadoCodigo, tieneAlerta, pageable)
                .map(this::toResponse)
        );
    }

    public VentaResponse obtener(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public PageResponse<VentaResponse> porCliente(UUID clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new NotFoundException("Cliente no encontrado: " + clienteId);
        }
        return PageResponse.of(
            ventaRepository.findByClienteId(clienteId, pageable).map(this::toResponse)
        );
    }
    private LocalDate resolverFechaInicio(String periodo) {
        LocalDate hoy = LocalDate.now();
        return switch (periodo) {
            case "7d"  -> hoy.minusDays(7);
            case "mes" -> hoy.withDayOfMonth(1);
            default    -> hoy.minusDays(15);  
        };
    }
    @Transactional
    public VentaResponse crear(VentaRequest req) {
        Usuario agente = getUsuarioAutenticado();
        
        Campana campana = campanaRepository.findById(req.getCampanaId())
            .orElseThrow(() -> new NotFoundException("Campaña no encontrada"));

        EstadoVenta estadoInicial = estadoVentaRepository.findByCodigo("EN_PROCESO")
            .orElseThrow(() -> new NotFoundException("Estado ACTIVO no encontrado"));

        Cliente cliente = null;
        String clienteNombre = req.getClienteNombre();
        String clienteDoc    = req.getClienteDoc();
        String clienteTel    = req.getClienteTelefono();

        if (req.getClienteId() != null) {
            cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + req.getClienteId()));
            clienteNombre = cliente.getNombre() + " " + cliente.getApellidoP()+ " " + cliente.getApellidoM();
            clienteDoc    = cliente.getNroDoc();
            clienteTel    = cliente.getTelefono();
        } else if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new BusinessException("Se requiere clienteId o clienteNombre");
        }
        Producto producto = null;
        if (req.getProductoId() != null) {
            producto = productoRepository.findById(req.getProductoId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + req.getProductoId()));

            if (req.getMonto() == null) {
                req.setMonto(producto.getPrecio());
            }
        }

        Venta venta = Venta.builder()
            .campana(campana)
            .agente(agente)
            .cliente(cliente)
            .estado(estadoInicial)
            .producto(producto)  
            .clienteNombre(clienteNombre)
            .clienteDoc(clienteDoc)
            .clienteTelefono(clienteTel)
            .fechaVenta(req.getFechaVenta())
            .monto(req.getMonto())
            .observaciones(req.getObservaciones())
            .tieneAlerta(false)
            .eliminado(false)
            .creadoPor(agente)
            .build();

        Venta ventaGuardada = ventaRepository.saveAndFlush(venta);

        return toResponse(ventaGuardada);
    }

    @Transactional
    public VentaResponse cambiarEstado(UUID id, CambioEstadoRequest req) {
        Venta venta = findOrThrow(id);
        EstadoVenta nuevoEstado = estadoVentaRepository.findByCodigo(req.getEstadoCodigo())
            .orElseThrow(() -> new NotFoundException("Estado no encontrado: " + req.getEstadoCodigo()));

        if (venta.getEstado().getEsFinal()) {
            throw new BusinessException("La venta está en estado final y no puede modificarse");
        }

        venta.setEstado(nuevoEstado);
        venta.setActualizadoPor(getUsuarioAutenticado());
        venta.setAlertaDetalle(req.getMotivo());

        switch (req.getEstadoCodigo()) {
            case "ACTIVO" -> {
                venta.setTieneAlerta(true);
                venta.setAlertaDetalle(null); // ← limpia observación anterior
                venta.setAlertaExpiraEn(OffsetDateTime.now().plusMinutes(1));
            }
            case "OBSERVADO" -> {
                venta.setTieneAlerta(true);
                venta.setAlertaExpiraEn(null); // sin expiración
            }
            case "CAIDA" -> {
                venta.setTieneAlerta(true);
                venta.setAlertaExpiraEn(null); // sin expiración, se archiva manualmente
            }
            case "EN_PROCESO" -> {
                venta.setTieneAlerta(false);
                venta.setAlertaExpiraEn(null);
            }
        }

        return toResponse(ventaRepository.save(venta));
    }
    @Transactional
    public void archivarCaida(UUID id) {
        Venta venta = findOrThrow(id);
        if (!"CAIDA".equals(venta.getEstado().getCodigo())) {
            throw new BusinessException("Solo se pueden archivar ventas en estado CAÍDA");
        }
        venta.setTieneAlerta(false);
        ventaRepository.save(venta);
    }
    @Transactional
    public VentaResponse actualizarCliente(UUID ventaId, UUID clienteId) {
        Venta venta = findOrThrow(ventaId);
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));
        venta.setCliente(cliente);
        venta.setActualizadoPor(getUsuarioAutenticado());
        return toResponse(ventaRepository.save(venta));
    }

    @Transactional
    public void eliminar(UUID id) {
        Venta venta = findOrThrow(id);
        venta.setEliminado(true);
        venta.setEliminadoEn(OffsetDateTime.now());
        venta.setEliminadoPor(getUsuarioAutenticado());
        ventaRepository.save(venta);
    }


    private Venta findOrThrow(UUID id) {
        return ventaRepository.findByIdAndEliminadoFalse(id)
            .orElseThrow(() -> new NotFoundException("Venta no encontrada: " + id));
    }

    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }

    private VentaResponse toResponse(Venta v) {
        Cliente c = v.getCliente();
        return VentaResponse.builder()
            .id(v.getId())
            .codigoVenta(v.getCodigoVenta())
            .fechaVenta(v.getFechaVenta())
            .monto(v.getMonto())
            .observaciones(v.getObservaciones())
            .tieneAlerta(v.getTieneAlerta())
            .alertaDetalle(v.getAlertaDetalle())
            .creadoEn(v.getCreadoEn())
            .actualizadoEn(v.getActualizadoEn())
            // Campaña
            .campanaId(v.getCampana().getId())
            .campanaNombre(v.getCampana().getNombre())
            .lineaNombre(v.getCampana().getLinea().getNombre())
            // Agente
            .agenteId(v.getAgente().getId())
            .agenteNombre(v.getAgente().getNombres() + " " + v.getAgente().getApellidos())
            // Estado
            .estadoCodigo(v.getEstado().getCodigo())
            .estadoNombre(v.getEstado().getNombre())

            .comisionGenerada(v.getComisionGenerada())   
            .comisionPorcentaje(v.getComisionPorcentaje())
            .clienteId(c != null ? c.getId() : null)
            .clienteNombre(c != null ? c.getNombre() + " " + c.getApellidoP()+ " " + c.getApellidoM() : v.getClienteNombre())
            .clienteDoc(c != null ? c.getNroDoc() : v.getClienteDoc())
            .clienteTelefono(c != null ? c.getTelefono() : v.getClienteTelefono())
            .clienteEmail(c != null ? c.getEmail() : null)
            .clienteDistrito(c != null ? c.getDistrito() : null)

            .productoId(v.getProducto() != null ? v.getProducto().getId() : null)
            .productoNombre(v.getProducto() != null ? v.getProducto().getNombre() : null)
            .productoPrecio(v.getProducto() != null ? v.getProducto().getPrecio() : null)
            .build();
    }

    public List<EstadoConteoResponse> getPorEstado(String periodo) {
        UUID agenteId   = getUsuarioAutenticado().getId();
        LocalDate desde = resolverFechaInicio(periodo);
 
        return ventaRepository.ventasPorEstado(agenteId, desde)
                .stream()
                .map(row -> EstadoConteoResponse.builder()
                        .estado(row.getEstado())
                        .codigo(row.getCodigo())
                        .total(row.getTotal())
                        .build())
                .toList();
    }
 
    /**
     * GET /api/ventas/alertas
     * Devuelve las ventas observadas del asesor autenticado.
     * Sin filtro de período: siempre muestra todas las alertas activas.
     */
    public List<AlertaVentaResponse> getAlertas() {
        UUID agenteId = getUsuarioAutenticado().getId();

        return ventaRepository.alertasActivas(agenteId, OffsetDateTime.now()) // ← agrega OffsetDateTime.now()
                .stream()
                .map(v -> AlertaVentaResponse.builder()
                        .id(v.getId())
                        .codigoVenta(v.getCodigoVenta())
                        .clienteNombre(v.getClienteNombre())
                        .alertaDetalle(v.getAlertaDetalle())
                        .estado(v.getEstado().getNombre())
                        .actualizadoEn(v.getActualizadoEn())
                        .alertaExpiraEn(v.getAlertaExpiraEn())
                        .build())
                .toList();
    }
    public List<HistorialEstadoResponse> getHistorial(UUID ventaId) {
        return historialEstadoRepository.findByVentaIdOrderByCambiadoEnDesc(ventaId)
            .stream()
            .map(h -> HistorialEstadoResponse.builder()
                .estadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getNombre() : "—")
                .estadoNuevo(h.getEstadoNuevo().getNombre())
                .motivo(h.getMotivo())
                .cambiadoEn(h.getCambiadoEn())
                .build())
            .toList();
    }

    @Transactional
    public VentaResponse actualizar(UUID id, VentaRequest req) {
        Venta venta = findOrThrow(id);

        if (req.getCampanaId() != null) {
            Campana campana = campanaRepository.findById(req.getCampanaId())
                .orElseThrow(() -> new NotFoundException("Campaña no encontrada"));
            venta.setCampana(campana);
        }

        if (req.getProductoId() != null) {
            Producto producto = productoRepository.findById(req.getProductoId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
            venta.setProducto(producto);
            if (req.getMonto() == null) {
                venta.setMonto(producto.getPrecio());
            }
        }

        if (req.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
            venta.setCliente(cliente);
            venta.setClienteNombre(cliente.getNombre() + " " + cliente.getApellidoP() + " " + cliente.getApellidoM());
            venta.setClienteDoc(cliente.getNroDoc());
            venta.setClienteTelefono(cliente.getTelefono());
        } else {
            if (req.getClienteNombre() != null) venta.setClienteNombre(req.getClienteNombre());
            if (req.getClienteDoc()    != null) venta.setClienteDoc(req.getClienteDoc());
            if (req.getClienteTelefono() != null) venta.setClienteTelefono(req.getClienteTelefono());
        }

        if (req.getFechaVenta()    != null) venta.setFechaVenta(req.getFechaVenta());
        if (req.getMonto()         != null) venta.setMonto(req.getMonto());
        if (req.getObservaciones() != null) venta.setObservaciones(req.getObservaciones());

        venta.setActualizadoPor(getUsuarioAutenticado());

        return toResponse(ventaRepository.save(venta));
    }
}
