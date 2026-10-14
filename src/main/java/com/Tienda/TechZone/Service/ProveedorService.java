package com.Tienda.TechZone.Service;

import com.Tienda.TechZone.Model.Proveedor;
import com.Tienda.TechZone.Repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<Proveedor> ListarProveedores() {
        return proveedorRepository.findAll();
    }

    public Proveedor CrearProveedor(Proveedor proveedor) {
        validarProveedor(proveedor);
        if (proveedor.getVigente() == null) {
            proveedor.setVigente(true);
        }
        return proveedorRepository.save(proveedor);
    }

    public Proveedor ActualizarProveedor(Long id, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        existente.setRazonSocial(proveedor.getRazonSocial());
        existente.setTipoDocumento(proveedor.getTipoDocumento());
        existente.setNumeroDocumento(proveedor.getNumeroDocumento());
        existente.setTelefonoContacto(proveedor.getTelefonoContacto());
        existente.setCorreo(proveedor.getCorreo());
        existente.setNombreEncargado(proveedor.getNombreEncargado());
        if (proveedor.getVigente() != null) {
            existente.setVigente(proveedor.getVigente());
        }
        validarProveedor(existente);
        return proveedorRepository.save(existente);
    }

    public void EliminarProveedor(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        proveedor.setVigente(false);
        proveedorRepository.save(proveedor);
    }

    public Proveedor ObtenerProveedorPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
    }

    private void validarProveedor(Proveedor proveedor) {
        if (proveedor.getRazonSocial() == null || proveedor.getRazonSocial().isBlank()) {
            throw new RuntimeException("La razon social es obligatoria");
        }
    }
}
