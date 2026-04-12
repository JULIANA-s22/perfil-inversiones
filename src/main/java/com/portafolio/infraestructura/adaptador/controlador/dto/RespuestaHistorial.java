package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.RegistroSimulacion;
import java.time.LocalDateTime;
import java.util.UUID;

public record RespuestaHistorial(UUID id, long capitalActual, long aporteMensual, int tiempoAnios,
        long valorConservador, long valorModerado, long valorAgresivo, LocalDateTime creadoEn) {
    public static RespuestaHistorial desde(RegistroSimulacion r) {
        return new RespuestaHistorial(r.getId(), r.getCapitalActual(), r.getAporteMensual(),
                r.getTiempoAnios(), r.getValorConservador(), r.getValorModerado(), r.getValorAgresivo(), r.getCreadoEn());
    }
}
