package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import com.portafolio.dominio.puerto.salida.RepositorioSimulacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component @RequiredArgsConstructor
public class AdaptadorPersistenciaSimulacion implements RepositorioSimulacion {
    private final RepositorioSimulacionSpring repositorioJpa;

    @Override
    public RegistroSimulacion guardar(RegistroSimulacion registro) {
        return repositorioJpa.save(EntidadSimulacionJpa.desdeDominio(registro)).aDominio();
    }
    @Override
    public List<RegistroSimulacion> buscarPorCorreo(String correo) {
        return repositorioJpa.findByCorreoUsuarioOrderByCreadoEnDesc(correo).stream()
                .map(EntidadSimulacionJpa::aDominio).toList();
    }
}
