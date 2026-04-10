package com.portafolio.infraestructura.adaptador.controlador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PeticionInicioSesion(
        @NotBlank @Email String correo,
        @NotBlank String contrasena
) {}
