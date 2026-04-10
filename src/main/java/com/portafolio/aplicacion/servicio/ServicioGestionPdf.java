package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.DocumentoPdf;
import com.portafolio.dominio.modelo.SolicitudGeneracionPdf;
import com.portafolio.dominio.puerto.entrada.CasoUsoGestionPdf;
import com.portafolio.dominio.puerto.salida.RepositorioDocumentoPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicioGestionPdf implements CasoUsoGestionPdf {

    private final RepositorioDocumentoPdf repositorioDocumentoPdf;

    @Override
    public DocumentoPdf solicitar(SolicitudGeneracionPdf solicitud) {
        // TODO: validar umbral del 1% entre valorFinalFront y cálculo propio
        // TODO: publicar a la cola
        DocumentoPdf documento = DocumentoPdf.crear(solicitud.usuarioId(), solicitud.valorFinalFront() + "");
        return repositorioDocumentoPdf.guardar(documento);
    }

    @Override
    public List<DocumentoPdf> listarPorUsuario(UUID usuarioId) {
        return repositorioDocumentoPdf.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public void iniciarProcesamiento(Integer id) {
        DocumentoPdf documento = repositorioDocumentoPdf.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("PDF no encontrado: " + id));
        documento.iniciarProcesamiento();
        repositorioDocumentoPdf.guardar(documento);
    }

    @Override
    public void completar(Integer id, String urlBucket) {
        DocumentoPdf documento = repositorioDocumentoPdf.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("PDF no encontrado: " + id));
        documento.completar(urlBucket);
        repositorioDocumentoPdf.guardar(documento);
    }

    @Override
    public void marcarError(Integer id) {
        DocumentoPdf documento = repositorioDocumentoPdf.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("PDF no encontrado: " + id));
        documento.marcarError();
        repositorioDocumentoPdf.guardar(documento);
    }
}
