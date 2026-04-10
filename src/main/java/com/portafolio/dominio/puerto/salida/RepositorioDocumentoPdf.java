package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.DocumentoPdf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioDocumentoPdf {
    DocumentoPdf guardar(DocumentoPdf documento);
    Optional<DocumentoPdf> buscarPorId(Integer id);
    List<DocumentoPdf> buscarPorUsuarioId(UUID usuarioId);
}