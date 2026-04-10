package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;

public interface ProveedorToken {
    TokenAutenticacion generarToken(Usuario usuario);
    String extraerCorreo(String token);
    boolean esValido(String token);
}
