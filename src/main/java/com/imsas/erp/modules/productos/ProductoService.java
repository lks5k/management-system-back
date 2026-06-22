package com.imsas.erp.modules.productos;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.exceptions.BusinessException;
import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findAllByActivoTrue(pageable)
                .map(ProductoResponse::from);
    }

    
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(UUID id) {
        return ProductoResponse.from(findActivoOrThrow(id));
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    
    @Transactional
    public ProductoResponse crear(ProductoRequest request, Usuario autenticado) {
        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, null);

        Producto producto = Producto.builder()
                .nombre(nombreNorm)
                .build();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: '{}' [{}] por {}",
                guardado.getNombre(), guardado.getId(), autenticado.getEmail());

        return ProductoResponse.from(guardado);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    
    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request, Usuario autenticado) {
        Producto producto = findActivoOrThrow(id);

        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, id);

        producto.setNombre(nombreNorm);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto actualizado: '{}' [{}] por {}",
                guardado.getNombre(), guardado.getId(), autenticado.getEmail());

        return ProductoResponse.from(guardado);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Producto producto = findActivoOrThrow(id);
        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto desactivado: '{}' [{}] por {}",
                producto.getNombre(), producto.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    
    private Producto findActivoOrThrow(UUID id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));
    }

    
    private void verificarNombreUnico(String nombreNorm, UUID excluirId) {
        boolean existe = excluirId == null
                ? productoRepository.existsByNombre(nombreNorm)
                : productoRepository.existsByNombreAndIdNot(nombreNorm, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_PRODUCTO",
                    "Ya existe un producto con el nombre '" + nombreNorm + "'");
        }
    }

    
    private String normalizar(String nombre) {
        return nombre.trim().toUpperCase();
    }
}
