package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProyeccionAnual {
    private final int anio;
    private final long pasivo;
    private final long proyectado;
}
