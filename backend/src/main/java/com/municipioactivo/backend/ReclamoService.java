package com.municipioactivo.backend;

import java.util.List;
import java.util.Optional;

public interface ReclamoService {

    Reclamo guardarReclamo(Reclamo reclamo);

    List<Reclamo> obtenerTodosLosReclamos();

    List<Reclamo> obtenerReclamosPendientes();

    List<Reclamo> obtenerReclamosPorEstado(String estado);

    Optional<Reclamo> buscarPorId(Long id);

    Optional<Reclamo> buscarPorCodigoSeguimiento(String codigoSeguimiento);
}
