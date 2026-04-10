package com.portafolio.dominio.modelo;

import java.util.UUID;

public record SolicitudGeneracionPdf(
        UUID usuarioId,
        double capitalActual,
        double aporteMensual,
        int cantidadAnios,
        PerfilInversion perfil,
        String graficaBase64,
        double valorFinalFront
) {
    public enum PerfilInversion {
        CONSERVADOR, MODERADO, AGRESIVO
    }
}
