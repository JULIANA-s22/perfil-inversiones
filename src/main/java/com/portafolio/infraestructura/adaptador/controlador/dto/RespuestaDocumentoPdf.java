package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.DocumentoPdf;

import java.time.LocalDateTime;

public record RespuestaDocumentoPdf(
        Integer id,
        String urlBucket,
        String jobId,
        String estadoGeneracion,
        LocalDateTime solicitadoEn
) {
    public static RespuestaDocumentoPdf desde(DocumentoPdf documento) {
        return new RespuestaDocumentoPdf(
                documento.getId(),
                documento.getUrlBucket(),
                documento.getJobId(),
                documento.getEstadoGeneracion().name(),
                documento.getSolicitadoEn()
        );
    }
}
