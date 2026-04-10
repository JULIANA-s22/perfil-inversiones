package com.portafolio.infraestructura.configuracion.seguridad;

import com.portafolio.dominio.puerto.salida.EncriptadorContrasena;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncriptadorBcrypt implements EncriptadorContrasena {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encriptar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaEncriptada) {
        return encoder.matches(contrasenaPlana, contrasenaEncriptada);
    }
}
