package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface RepositorioUsuario {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorCorreo(String correo);
    Optional<Usuario> buscarPorId(UUID id);
    boolean existePorCorreo(String correo);
}
