package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoPdf {

    private Integer id;
    private UUID usuarioId;
    private String urlBucket;
    private String jobId;
    @Builder.Default
    private EstadoGeneracion estadoGeneracion = EstadoGeneracion.PENDIENTE;
    @Builder.Default
    private LocalDateTime solicitadoEn = LocalDateTime.now();

    public enum EstadoGeneracion {
        PENDIENTE, PROCESANDO, COMPLETADO, ERROR
    }

    public static DocumentoPdf crear(UUID usuarioId, String jobId) {
        return DocumentoPdf.builder()
                .usuarioId(usuarioId)
                .jobId(jobId)
                .build();
    }

    public void iniciarProcesamiento() {
        this.estadoGeneracion = EstadoGeneracion.PROCESANDO;
    }

    public void completar(String urlBucket) {
        this.urlBucket = urlBucket;
        this.estadoGeneracion = EstadoGeneracion.COMPLETADO;
    }

    public void marcarError() {
        this.estadoGeneracion = EstadoGeneracion.ERROR;
    }
}
