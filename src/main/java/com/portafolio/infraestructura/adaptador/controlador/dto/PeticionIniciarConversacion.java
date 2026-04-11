package com.portafolio.infraestructura.adaptador.controlador.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PeticionIniciarConversacion(@NotNull UUID usuarioId) {}
