package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.SolicitudGeneracionPdf;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PeticionSolicitudPdf(
        @NotNull UUID usuarioId,
        @Positive double capitalActual,
        @Positive double aporteMensual,
        @Min(1) int cantidadAnios,
        @NotNull SolicitudGeneracionPdf.PerfilInversion perfil,
        @NotBlank String graficaBase64,
        @Positive double valorFinalFront
) {
    public SolicitudGeneracionPdf aDominio() {
        return new SolicitudGeneracionPdf(
                usuarioId, capitalActual, aporteMensual,
                cantidadAnios, perfil, graficaBase64, valorFinalFront
        );
    }
}
