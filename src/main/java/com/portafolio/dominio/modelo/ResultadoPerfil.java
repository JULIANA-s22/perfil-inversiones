package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultadoPerfil {
    private final String nombre;
    private final double tasaAnual;
    private final long valorFuturo;
    private final String etiqueta;
}
