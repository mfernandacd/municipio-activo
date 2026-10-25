package com.municipioactivo.backend;

import java.util.List;
import java.util.Optional;

public interface ReclamoService {

    Reclamo guardarReclamo(Reclamo reclamo);

    Reclamo registrarReclamo(Reclamo reclamo, String emailUsuario);

    List<Reclamo> obtenerTodosLosReclamos();

    List<Reclamo> obtenerReclamosPendientes();

    List<Reclamo> obtenerReclamosPorEstado(String estado);

    List<Reclamo> obtenerPorBarrioYZona(String barrio, String zona);

    Optional<Reclamo> buscarPorId(Long id);

    Optional<Reclamo> buscarPorCodigoSeguimiento(String codigoSeguimiento);
}
