package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.ProyeccionAnual;
import com.portafolio.dominio.modelo.ResultadoPerfil;
import com.portafolio.dominio.modelo.ResultadoSimulacion;
import com.portafolio.dominio.puerto.entrada.CasoUsoSimulacion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioSimulacion implements CasoUsoSimulacion {

    private static final double TASA_CONSERVADOR = 0.03;
    private static final double TASA_MODERADO = 0.07;
    private static final double TASA_AGRESIVO = 0.11;

    @Override
    public ResultadoSimulacion simular(long capitalActual, long aporteMensual, int tiempoAnios) {
        validarParametros(capitalActual, aporteMensual, tiempoAnios);

        ResultadoPerfil conservador = calcularPerfil("Conservador", TASA_CONSERVADOR, capitalActual, aporteMensual, tiempoAnios, "ESTABILIDAD 3% TEA");
        ResultadoPerfil moderado = calcularPerfil("Moderado", TASA_MODERADO, capitalActual, aporteMensual, tiempoAnios, "EQUILIBRIO 7% TEA");
        ResultadoPerfil agresivo = calcularPerfil("Agresivo", TASA_AGRESIVO, capitalActual, aporteMensual, tiempoAnios, "CRECIMIENTO 11% TEA");

        List<ProyeccionAnual> proyConservador = calcularProyeccionAnual(capitalActual, aporteMensual, tiempoAnios, TASA_CONSERVADOR);
        List<ProyeccionAnual> proyModerado = calcularProyeccionAnual(capitalActual, aporteMensual, tiempoAnios, TASA_MODERADO);
        List<ProyeccionAnual> proyAgresivo = calcularProyeccionAnual(capitalActual, aporteMensual, tiempoAnios, TASA_AGRESIVO);

        return new ResultadoSimulacion(capitalActual, aporteMensual, tiempoAnios,
                conservador, moderado, agresivo,
                proyConservador, proyModerado, proyAgresivo);
    }

    private ResultadoPerfil calcularPerfil(String nombre, double tasaAnual, long capital, long aporteMensual, int anios, String etiqueta) {
        double tasaMensual = tasaAnual / 12;
        int meses = anios * 12;

        double factor = Math.pow(1 + tasaMensual, meses);
        double valorFuturo = capital * factor + aporteMensual * ((factor - 1) / tasaMensual);

        return new ResultadoPerfil(nombre, tasaAnual * 100, Math.round(valorFuturo), etiqueta);
    }

    private List<ProyeccionAnual> calcularProyeccionAnual(long capital, long aporteMensual, int tiempoAnios, double tasaAnual) {
        List<ProyeccionAnual> proyeccion = new ArrayList<>();
        double tasaMensual = tasaAnual / 12;

        for (int anio = 0; anio <= tiempoAnios; anio++) {
            int meses = anio * 12;
            long pasivo = capital + (aporteMensual * meses);

            double factor = Math.pow(1 + tasaMensual, meses);
            long proyectado = meses == 0 ? capital : Math.round(capital * factor + aporteMensual * ((factor - 1) / tasaMensual));

            proyeccion.add(new ProyeccionAnual(anio, pasivo, proyectado));
        }

        return proyeccion;
    }

    private void validarParametros(long capitalActual, long aporteMensual, int tiempoAnios) {
        if (capitalActual < 0) throw new IllegalArgumentException("El capital actual no puede ser negativo");
        if (aporteMensual < 0) throw new IllegalArgumentException("El aporte mensual no puede ser negativo");
        if (tiempoAnios < 1 || tiempoAnios > 50) throw new IllegalArgumentException("El tiempo debe estar entre 1 y 50 años");
    }
}
