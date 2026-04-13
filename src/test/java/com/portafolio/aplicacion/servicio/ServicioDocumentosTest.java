package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.UrlPresignada;
import com.portafolio.dominio.puerto.salida.GeneradorUrlPresignada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioDocumentosTest {

    @Mock
    GeneradorUrlPresignada generadorUrlPresignada;

    @InjectMocks
    ServicioDocumentos servicio;

    @Test
    void obtenerUrlSubida_debeRetornarUrlPresignada_cuandoTipoEsPdf() {
        // ARRANGE
        String nombreArchivo = "reporte.pdf";
        String tipoContenido = "application/pdf";
        UrlPresignada urlEsperada = new UrlPresignada(
                "https://s3.amazonaws.com/bucket/pdfs/reporte.pdf?X-Amz-Signature=abc",
                "pdfs/reporte.pdf"
        );

        when(generadorUrlPresignada.generarUrlSubida(nombreArchivo, tipoContenido))
                .thenReturn(urlEsperada);

        // ACT
        UrlPresignada resultado = servicio.obtenerUrlSubida(nombreArchivo, tipoContenido);

        // ASSERT
        assertThat(resultado).isEqualTo(urlEsperada);
    }

    @Test
    void obtenerUrlSubida_debeLanzarExcepcion_cuandoTipoNoEsPdf() {
        // ARRANGE
        String nombreArchivo = "foto.png";
        String tipoContenido = "image/png";

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.obtenerUrlSubida(nombreArchivo, tipoContenido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se permiten archivos PDF");
    }

    @Test
    void obtenerUrlSubida_debeDelegarAlGeneradorConLosParametrosCorrectos() {
        // ARRANGE
        String nombreArchivo = "reporte.pdf";
        String tipoContenido = "application/pdf";

        when(generadorUrlPresignada.generarUrlSubida(nombreArchivo, tipoContenido))
                .thenReturn(new UrlPresignada("https://s3.amazonaws.com/...", "pdfs/reporte.pdf"));

        // ACT
        servicio.obtenerUrlSubida(nombreArchivo, tipoContenido);

        // ASSERT — verifica que el generador fue llamado con los parámetros exactos
        verify(generadorUrlPresignada).generarUrlSubida(nombreArchivo, tipoContenido);
    }
}
