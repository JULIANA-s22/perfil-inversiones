package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import java.util.List;

public interface RepositorioSimulacion {
    RegistroSimulacion guardar(RegistroSimulacion registro);
    List<RegistroSimulacion> buscarPorCorreo(String correo);
}
