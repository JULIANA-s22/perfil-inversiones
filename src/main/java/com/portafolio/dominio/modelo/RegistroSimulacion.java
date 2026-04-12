package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Builder @AllArgsConstructor
public class RegistroSimulacion {
    private final UUID id;
    private final String correoUsuario;
    private final long capitalActual;
    private final long aporteMensual;
    private final int tiempoAnios;
    private final long valorConservador;
    private final long valorModerado;
    private final long valorAgresivo;
    private final LocalDateTime creadoEn;
}
