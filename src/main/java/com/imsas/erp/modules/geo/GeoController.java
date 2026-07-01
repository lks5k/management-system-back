package com.imsas.erp.modules.geo;

import com.imsas.erp.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints públicos de datos geográficos de referencia (sin autenticación).
 * Los datos son estáticos (cargados por Flyway) y se pueden cachear agresivamente en el cliente.
 *
 * GET /api/v1/geo/paises                        → todos los países ordenados por nombre
 * GET /api/v1/geo/departamentos?paisId={id}      → departamentos del país
 * GET /api/v1/geo/ciudades?departamentoId={id}   → ciudades del departamento
 */
@RestController
@RequestMapping("/api/v1/geo")
@RequiredArgsConstructor
public class GeoController {

    private final PaisRepository        paisRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CiudadRepository      ciudadRepository;

    // ─── GET /api/v1/geo/paises ───────────────────────────────────────────────

    @GetMapping("/paises")
    public ResponseEntity<ApiResponse<List<GeoItemResponse>>> listarPaises() {
        List<GeoItemResponse> result = paisRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(GeoItemResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // ─── GET /api/v1/geo/departamentos?paisId={id} ────────────────────────────

    @GetMapping("/departamentos")
    public ResponseEntity<ApiResponse<List<GeoItemResponse>>> listarDepartamentos(
            @RequestParam Short paisId
    ) {
        List<GeoItemResponse> result = departamentoRepository.findByPaisIdOrderByNombreAsc(paisId)
                .stream()
                .map(GeoItemResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // ─── GET /api/v1/geo/ciudades?departamentoId={id} ─────────────────────────

    @GetMapping("/ciudades")
    public ResponseEntity<ApiResponse<List<GeoItemResponse>>> listarCiudades(
            @RequestParam Short departamentoId
    ) {
        List<GeoItemResponse> result = ciudadRepository.findByDepartamentoIdOrderByNombreAsc(departamentoId)
                .stream()
                .map(GeoItemResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }
}
