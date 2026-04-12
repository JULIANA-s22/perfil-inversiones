package com.portafolio.infraestructura.adaptador.mensajeria;

import lombok.*;
import java.io.Serializable;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class MensajeSimulacion implements Serializable {
    private String correoDestino;
    private long capitalActual;
    private long aporteMensual;
    private int tiempoAnios;
    private long valorConservador;
    private long valorModerado;
    private long valorAgresivo;
}
