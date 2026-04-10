package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.Usuario;
import com.portafolio.dominio.puerto.salida.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdaptadorPersistenciaUsuario implements RepositorioUsuario {

    private final RepositorioUsuarioSpring repositorioJpa;

    @Override
    public Usuario guardar(Usuario usuario) {
        EntidadUsuarioJpa entidad = EntidadUsuarioJpa.desdeDominio(usuario);
        return repositorioJpa.save(entidad).aDominio();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repositorioJpa.findByCorreo(correo).map(EntidadUsuarioJpa::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return repositorioJpa.findById(id).map(EntidadUsuarioJpa::aDominio);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repositorioJpa.existsByCorreo(correo);
    }
}
