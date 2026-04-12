package com.portafolio.infraestructura.adaptador.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RepositorioSimulacionSpring extends JpaRepository<EntidadSimulacionJpa, UUID> {
    List<EntidadSimulacionJpa> findByCorreoUsuarioOrderByCreadoEnDesc(String correoUsuario);
}
