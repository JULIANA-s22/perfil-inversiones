package com.portafolio.infraestructura.adaptador.controlador.dto;

import jakarta.validation.constraints.NotBlank;

public record PeticionEnviarMensaje(@NotBlank String contenido) {}
