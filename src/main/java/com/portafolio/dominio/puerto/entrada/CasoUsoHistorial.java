package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import java.util.List;

public interface CasoUsoHistorial {
    RegistroSimulacion guardar(String correoUsuario, long capitalActual, long aporteMensual,
                                int tiempoAnios, long valorConservador, long valorModerado, long valorAgresivo);
    List<RegistroSimulacion> obtenerHistorial(String correoUsuario);
}
