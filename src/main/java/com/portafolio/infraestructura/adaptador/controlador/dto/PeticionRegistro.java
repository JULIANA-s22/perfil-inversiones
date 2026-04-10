package com.portafolio.infraestructura.adaptador.controlador.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PeticionRegistro(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres") String contrasena,
        @NotBlank String nombreCompleto
) {}
