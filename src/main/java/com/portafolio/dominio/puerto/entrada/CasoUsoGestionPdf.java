package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.DocumentoPdf;
import com.portafolio.dominio.modelo.SolicitudGeneracionPdf;

import java.util.List;
import java.util.UUID;

public interface CasoUsoGestionPdf {
    DocumentoPdf solicitar(SolicitudGeneracionPdf solicitud);
    List<DocumentoPdf> listarPorUsuario(UUID usuarioId);
    void iniciarProcesamiento(Integer id);
    void completar(Integer id, String urlBucket);
    void marcarError(Integer id);
}
