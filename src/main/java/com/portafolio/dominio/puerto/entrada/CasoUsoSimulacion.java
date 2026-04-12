package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.ResultadoSimulacion;

public interface CasoUsoSimulacion {
    ResultadoSimulacion simular(long capitalActual, long aporteMensual, int tiempoAnios);
}
