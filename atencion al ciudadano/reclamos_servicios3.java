package com.municipio.serranoble.service;

import com.municipio.serranoble.entity.Reclamo;
import java.util.List;

public interface ReclamoService {
    Reclamo guardarReclamo(Reclamo reclamo);
    List<Reclamo> obtenerTodosLosReclamos();
    List<Reclamo> obtenerReclamosPendientes();
}