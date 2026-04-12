package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import com.portafolio.dominio.puerto.entrada.CasoUsoHistorial;
import com.portafolio.dominio.puerto.salida.RepositorioSimulacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ServicioHistorial implements CasoUsoHistorial {
    private final RepositorioSimulacion repositorio;

    @Override
    public RegistroSimulacion guardar(String correoUsuario, long capitalActual, long aporteMensual,
                                       int tiempoAnios, long valorConservador, long valorModerado, long valorAgresivo) {
        return repositorio.guardar(RegistroSimulacion.builder()
                .id(UUID.randomUUID()).correoUsuario(correoUsuario)
                .capitalActual(capitalActual).aporteMensual(aporteMensual).tiempoAnios(tiempoAnios)
                .valorConservador(valorConservador).valorModerado(valorModerado).valorAgresivo(valorAgresivo)
                .creadoEn(LocalDateTime.now()).build());
    }

    @Override
    public List<RegistroSimulacion> obtenerHistorial(String correoUsuario) {
        return repositorio.buscarPorCorreo(correoUsuario);
    }
}
