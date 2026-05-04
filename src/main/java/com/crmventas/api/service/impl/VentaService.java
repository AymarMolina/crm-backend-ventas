package com.crmventas.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.dto.request.CambioEstadoRequest;
import com.crmventas.api.dto.request.VentaRequest;
import com.crmventas.api.dto.response.PageResponse;
import com.crmventas.api.dto.response.VentaResponse;
import com.crmventas.api.entity.Campana;
import com.crmventas.api.entity.Cliente;
import com.crmventas.api.entity.EstadoVenta;
import com.crmventas.api.entity.Usuario;
import com.crmventas.api.entity.Venta;
import com.crmventas.api.exception.BusinessException;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.CampanaRepository;
import com.crmventas.api.repository.ClienteRepository;
import com.crmventas.api.repository.EstadoVentaRepository;
import com.crmventas.api.repository.UsuarioRepository;
import com.crmventas.api.repository.VentaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final CampanaRepository campanaRepository;
    private final EstadoVentaRepository estadoVentaRepository;
    private final UsuarioRepository usuarioRepository;

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

    @Transactional
    public VentaResponse crear(VentaRequest req) {
        Usuario agente = getUsuarioAutenticado();

        if (ventaRepository.existsByCodigoVenta(req.getCodigoVenta())) {
            throw new BusinessException("El código de venta ya existe: " + req.getCodigoVenta());
        }

        Campana campana = campanaRepository.findById(req.getCampanaId())
            .orElseThrow(() -> new NotFoundException("Campaña no encontrada"));

        EstadoVenta estadoInicial = estadoVentaRepository.findByCodigo("ACTIVO")
            .orElseThrow(() -> new NotFoundException("Estado ACTIVO no encontrado"));

        // Resolver cliente
        Cliente cliente = null;
        String clienteNombre = req.getClienteNombre();
        String clienteDoc    = req.getClienteDoc();
        String clienteTel    = req.getClienteTelefono();

        if (req.getClienteId() != null) {
            cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + req.getClienteId()));
            clienteNombre = cliente.getNombre() + " " + cliente.getApellidos();
            clienteDoc    = cliente.getNroDoc();
            clienteTel    = cliente.getTelefono();
        } else if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new BusinessException("Se requiere clienteId o clienteNombre");
        }

        Venta venta = Venta.builder()
            .campana(campana)
            .agente(agente)
            .cliente(cliente)
            .estado(estadoInicial)
            .codigoVenta(req.getCodigoVenta())
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

        return toResponse(ventaRepository.save(venta));
    }

    @Transactional
    public VentaResponse cambiarEstado(UUID id, CambioEstadoRequest req) {
        Venta venta = findOrThrow(id);
        EstadoVenta nuevoEstado = estadoVentaRepository.findByCodigo(req.getEstadoCodigo())
            .orElseThrow(() -> new NotFoundException("Estado no encontrado: " + req.getEstadoCodigo()));

        // No se puede cambiar desde un estado final (CAIDA)
        if (venta.getEstado().getEsFinal()) {
            throw new BusinessException("La venta está en estado final y no puede modificarse");
        }

        venta.setEstado(nuevoEstado);
        venta.setActualizadoPor(getUsuarioAutenticado());

        // Si se marca como OBSERVADO, activar alerta
        if ("OBSERVADO".equals(req.getEstadoCodigo())) {
            venta.setTieneAlerta(true);
            venta.setAlertaDetalle(req.getMotivo());
        }

        return toResponse(ventaRepository.save(venta));
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

    // ── helpers ──────────────────────────────────────────────────────────────

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
            // Cliente resuelto (ficha tiene prioridad sobre campos sueltos)
            .clienteId(c != null ? c.getId() : null)
            .clienteNombre(c != null ? c.getNombre() + " " + c.getApellidos() : v.getClienteNombre())
            .clienteDoc(c != null ? c.getNroDoc() : v.getClienteDoc())
            .clienteTelefono(c != null ? c.getTelefono() : v.getClienteTelefono())
            .clienteEmail(c != null ? c.getEmail() : null)
            .clienteDistrito(c != null ? c.getDistrito() : null)
            .build();
    }
}
