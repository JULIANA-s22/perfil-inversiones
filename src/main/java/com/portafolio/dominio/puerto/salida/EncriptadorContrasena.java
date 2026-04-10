package com.portafolio.dominio.puerto.salida;

public interface EncriptadorContrasena {
    String encriptar(String contrasenaPlana);
    boolean coincide(String contrasenaPlana, String contrasenaEncriptada);
}
