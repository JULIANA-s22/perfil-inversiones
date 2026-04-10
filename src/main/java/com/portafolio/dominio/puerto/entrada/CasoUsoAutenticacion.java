package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.TokenAutenticacion;
import com.portafolio.dominio.modelo.Usuario;

public interface CasoUsoAutenticacion {
    Usuario registrar(String correo, String contrasena, String nombreCompleto);
    TokenAutenticacion iniciarSesion(String correo, String contrasena);
    TokenAutenticacion refrescarToken(String tokenRefresco);
}
