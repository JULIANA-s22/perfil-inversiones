package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.UrlPresignada;
import com.portafolio.dominio.puerto.entrada.CasoUsoDocumentos;
import com.portafolio.dominio.puerto.salida.GeneradorUrlPresignada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioDocumentos implements CasoUsoDocumentos {

    private final GeneradorUrlPresignada generadorUrlPresignada;

    @Override
    public UrlPresignada obtenerUrlSubida(String nombreArchivo, String tipoContenido) {
        if (!tipoContenido.equals("application/pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF");
        }
        return generadorUrlPresignada.generarUrlSubida(nombreArchivo, tipoContenido);
    }
}
