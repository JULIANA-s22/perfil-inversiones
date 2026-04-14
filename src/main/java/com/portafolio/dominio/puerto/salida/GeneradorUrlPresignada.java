package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.UrlPresignada;

public interface GeneradorUrlPresignada {
    UrlPresignada generarUrlSubida(String nombreArchivo, String tipoContenido);
}
