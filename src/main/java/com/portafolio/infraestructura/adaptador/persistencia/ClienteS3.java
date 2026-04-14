package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.UrlPresignada;
import com.portafolio.dominio.puerto.salida.GeneradorUrlPresignada;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ClienteS3 implements GeneradorUrlPresignada {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public UrlPresignada generarUrlSubida(String nombreArchivo, String tipoContenido) {
        String key = "pdfs/" + nombreArchivo;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(tipoContenido)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return new UrlPresignada(presignedRequest.url().toString(), key);
    }
}
