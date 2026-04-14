package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.UrlPresignada;

public interface CasoUsoDocumentos {
    UrlPresignada obtenerUrlSubida(String nombreArchivo, String tipoContenido);
}
