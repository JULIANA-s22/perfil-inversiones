package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.Usuario;

public record RespuestaUsuario(String id, String correo, String nombreCompleto) {

    public static RespuestaUsuario desde(Usuario usuario) {
        return new RespuestaUsuario(
                usuario.getId().toString(),
                usuario.getCorreo(),
                usuario.getNombreCompleto()
        );
    }
}
