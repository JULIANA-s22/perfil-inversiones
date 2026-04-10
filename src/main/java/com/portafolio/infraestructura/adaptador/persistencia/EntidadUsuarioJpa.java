package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntidadUsuarioJpa {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    private Usuario.Rol rol;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    public static EntidadUsuarioJpa desdeDominio(Usuario usuario) {
        return EntidadUsuarioJpa.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .contrasena(usuario.getContrasena())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .creadoEn(usuario.getCreadoEn())
                .build();
    }

    public Usuario aDominio() {
        return Usuario.builder()
                .id(id)
                .correo(correo)
                .contrasena(contrasena)
                .nombreCompleto(nombreCompleto)
                .rol(rol)
                .creadoEn(creadoEn)
                .build();
    }
}
