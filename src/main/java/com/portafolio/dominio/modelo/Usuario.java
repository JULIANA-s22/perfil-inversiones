package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private UUID id;
    private String correo;
    private String contrasena;
    private String nombreCompleto;
    @Builder.Default
    private Rol rol = Rol.USUARIO;
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    public enum Rol {
        USUARIO, ADMINISTRADOR
    }

    public static Usuario crear(String correo, String contrasena, String nombreCompleto) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .correo(correo)
                .contrasena(contrasena)
                .nombreCompleto(nombreCompleto)
                .build();
    }
}
