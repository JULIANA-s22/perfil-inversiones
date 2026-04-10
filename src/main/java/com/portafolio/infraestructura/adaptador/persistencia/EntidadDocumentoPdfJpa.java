package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.DocumentoPdf;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documentos_pdf")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntidadDocumentoPdfJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "url_bucket")
    private String urlBucket;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "estado_generacion", nullable = false, columnDefinition = "estado_pdf")
    private DocumentoPdf.EstadoGeneracion estadoGeneracion;

    @Column(name = "solicitado_en", nullable = false)
    private LocalDateTime solicitadoEn;

    public static EntidadDocumentoPdfJpa desdeDominio(DocumentoPdf documento) {
        return EntidadDocumentoPdfJpa.builder()
                .id(documento.getId())
                .usuarioId(documento.getUsuarioId())
                .urlBucket(documento.getUrlBucket())
                .jobId(documento.getJobId())
                .estadoGeneracion(documento.getEstadoGeneracion())
                .solicitadoEn(documento.getSolicitadoEn())
                .build();
    }

    public DocumentoPdf aDominio() {
        return DocumentoPdf.builder()
                .id(id)
                .usuarioId(usuarioId)
                .urlBucket(urlBucket)
                .jobId(jobId)
                .estadoGeneracion(estadoGeneracion)
                .solicitadoEn(solicitadoEn)
                .build();
    }
}
