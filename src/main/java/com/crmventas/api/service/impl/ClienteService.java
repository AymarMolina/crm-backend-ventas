package com.crmventas.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crmventas.api.dto.request.ClienteRequest;
import com.crmventas.api.dto.response.ClienteResponse;
import com.crmventas.api.dto.response.PageResponse;
import com.crmventas.api.entity.Cliente;
import com.crmventas.api.exception.ConflictException;
import com.crmventas.api.exception.NotFoundException;
import com.crmventas.api.repository.ClienteRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public PageResponse<ClienteResponse> listar(String q, Pageable pageable) {
        String filtro = (q != null && !q.isBlank()) ? q.trim() : null; // ← agregar esto
        return PageResponse.of(clienteRepository.buscar(filtro, pageable).map(this::toResponse));
    }

    public ClienteResponse obtener(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public ClienteResponse buscarPorDoc(String tipoDoc, String nroDoc) {
        return clienteRepository.findByTipoDocAndNroDoc(tipoDoc, nroDoc)
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest req) {
        if (clienteRepository.existsByTipoDocAndNroDoc(req.getTipoDoc(), req.getNroDoc())) {
            throw new ConflictException("Ya existe un cliente con ese documento");
        }
        Cliente c = Cliente.builder()
            .tipoDoc(req.getTipoDoc())
            .nroDoc(req.getNroDoc())
            .nombre(req.getNombre())
            .apellidoP(req.getApellidoP())
            .apellidoM(req.getApellidoM())
            .telefono(req.getTelefono())
            .telefonoAlt(req.getTelefonoAlt())
            .email(req.getEmail())
            .direccion(req.getDireccion())
            .distrito(req.getDistrito())
            .activo(true)
            .build();
        return toResponse(clienteRepository.save(c));
    }

    @Transactional
    public ClienteResponse actualizar(UUID id, ClienteRequest req) {
        Cliente c = findOrThrow(id);
        // Verificar conflicto de doc solo si cambió
        if (!c.getNroDoc().equals(req.getNroDoc()) || !c.getTipoDoc().equals(req.getTipoDoc())) {
            if (clienteRepository.existsByTipoDocAndNroDoc(req.getTipoDoc(), req.getNroDoc())) {
                throw new ConflictException("Ya existe un cliente con ese documento");
            }
        }
        c.setTipoDoc(req.getTipoDoc());
        c.setNroDoc(req.getNroDoc());
        c.setNombre(req.getNombre());
        c.setApellidoP(req.getApellidoP());
        c.setApellidoM(req.getApellidoM());
        c.setTelefono(req.getTelefono());
        c.setTelefonoAlt(req.getTelefonoAlt());
        c.setEmail(req.getEmail());
        c.setDireccion(req.getDireccion());
        c.setDistrito(req.getDistrito());
        return toResponse(clienteRepository.save(c));
    }

    @Transactional
    public void desactivar(UUID id) {
        Cliente c = findOrThrow(id);
        c.setActivo(false);
        clienteRepository.save(c);
    }

    private Cliente findOrThrow(UUID id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + id));
    }

    private ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
            .id(c.getId())
            .tipoDoc(c.getTipoDoc())
            .nroDoc(c.getNroDoc())
            .nombre(c.getNombre())
            .apellidoP(c.getApellidoP())
            .apellidoM(c.getApellidoM())
            .nombreCompleto(c.getNombre() + " " + c.getApellidoP()+ " " + c.getApellidoM())
            .telefono(c.getTelefono())
            .telefonoAlt(c.getTelefonoAlt())
            .email(c.getEmail())
            .direccion(c.getDireccion())
            .distrito(c.getDistrito())
            .activo(c.getActivo())
            .creadoEn(c.getCreadoEn())
            .build();
    }
}
