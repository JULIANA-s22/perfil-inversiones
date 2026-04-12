package com.portafolio.infraestructura.adaptador.controlador.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PeticionSimulacion(
        @NotNull(message = "El capital actual es obligatorio")
        @Min(value = 0, message = "El capital no puede ser negativo")
        Long capitalActual,

        @NotNull(message = "El aporte mensual es obligatorio")
        @Min(value = 0, message = "El aporte no puede ser negativo")
        Long aporteMensual,

        @NotNull(message = "El tiempo es obligatorio")
        @Min(value = 1, message = "Mínimo 1 año")
        @Max(value = 50, message = "Máximo 50 años")
        Integer tiempoAnios
) {}
