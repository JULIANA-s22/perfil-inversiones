package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.DocumentoPdf;
import com.portafolio.dominio.puerto.salida.RepositorioDocumentoPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdaptadorPersistenciaDocumentoPdf implements RepositorioDocumentoPdf {

    private final RepositorioDocumentoPdfSpring repositorioJpa;

    @Override
    public DocumentoPdf guardar(DocumentoPdf documento) {
        EntidadDocumentoPdfJpa entidad = EntidadDocumentoPdfJpa.desdeDominio(documento);
        return repositorioJpa.save(entidad).aDominio();
    }

    @Override
    public Optional<DocumentoPdf> buscarPorId(Integer id) {
        return repositorioJpa.findById(id).map(EntidadDocumentoPdfJpa::aDominio);
    }

    @Override
    public List<DocumentoPdf> buscarPorUsuarioId(UUID usuarioId) {
        return repositorioJpa.findByUsuarioId(usuarioId).stream()
                .map(EntidadDocumentoPdfJpa::aDominio)
                .toList();
    }
}
