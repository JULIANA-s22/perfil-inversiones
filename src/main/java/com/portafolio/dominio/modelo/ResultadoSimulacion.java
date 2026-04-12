package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResultadoSimulacion {
    private final long capitalActual;
    private final long aporteMensual;
    private final int tiempoAnios;
    private final ResultadoPerfil conservador;
    private final ResultadoPerfil moderado;
    private final ResultadoPerfil agresivo;
    private final List<ProyeccionAnual> proyeccionConservador;
    private final List<ProyeccionAnual> proyeccionModerado;
    private final List<ProyeccionAnual> proyeccionAgresivo;
}
