package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.ProyeccionAnual;
import com.portafolio.dominio.modelo.ResultadoSimulacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioSimulacionTest {

    private ServicioSimulacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioSimulacion();
    }

    // --- Grupo A: validaciones ---

    @Test
    void simular_debeLanzarExcepcion_cuandoCapitalEsNegativo() {
        // ARRANGE
        long capital = -1;
        long aporte = 1000;
        int tiempo = 10;

        // ACT + ASSERT
        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.simular(capital, aporte, tiempo)
        );
    }

    @Test
    void simular_debeLanzarExcepcion_cuandoAporteEsNegativo() {
        // ARRANGE
        long capital = 5000;
        long aporte = -500;
        int tiempo = 10;

        // ACT + ASSERT
        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.simular(capital, aporte, tiempo)
        );
    }

    @Test
    void simular_debeLanzarExcepcion_cuandoTiempoEsMenorQueUno() {
        // ARRANGE
        long capital = 5000;
        long aporte = 1000;
        int tiempo = 0;

        // ACT + ASSERT
        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.simular(capital, aporte, tiempo)
        );
    }

    @Test
    void simular_debeLanzarExcepcion_cuandoTiempoEsMayorQueCincuenta() {
        // ARRANGE
        long capital = 5000;
        long aporte = 1000;
        int tiempo = 51;

        // ACT + ASSERT
        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.simular(capital, aporte, tiempo)
        );
    }

    @Test
    void simular_noDebeLanzarExcepcion_cuandoTiempoEsUno() {
        // ARRANGE
        long capital = 5000;
        long aporte = 1000;
        int tiempo = 1;

        // ACT + ASSERT
        assertDoesNotThrow(
                () -> servicio.simular(capital, aporte, tiempo)
        );
    }

    @Test
    void simular_noDebeLanzarExcepcion_cuandoTiempoEsCincuenta() {
        // ARRANGE
        long capital = 5000;
        long aporte = 1000;
        int tiempo = 50;

        // ACT + ASSERT
        assertDoesNotThrow(
                () -> servicio.simular(capital, aporte, tiempo)
        );

    }

    // --- Grupo B: cálculo ---

    @Test
    void simular_debeRetornarResultado_cuandoParametrosSonValidos() {
        // ARRANGE
        long capital = 10_000;
        long aporte = 500;
        int tiempo = 5;

        // ACT
        ResultadoSimulacion resultado = servicio.simular(capital, aporte, tiempo);

        // ASSERT
        assertThat(resultado).isNotNull();
    }

    @Test
    void simular_debeRetornarValoresCrecientes_segunTasa() {
        // ARRANGE
        long capital = 10_000;
        long aporte = 500;
        int tiempo = 5;

        // ACT
        ResultadoSimulacion resultado = servicio.simular(capital, aporte, tiempo);

        // ASSERT
        assertThat(resultado.getAgresivo().getValorFuturo())
            .isGreaterThan(resultado.getModerado().getValorFuturo());

        assertThat(resultado.getModerado().getValorFuturo())
            .isGreaterThan(resultado.getConservador().getValorFuturo());
    }

    @Test
    void simular_debeRetornarProyeccionConTamanioAniosMasUno() {
        // ARRANGE
        long capital = 10_000;
        long aporte = 500;
        int tiempo = 5;

        // ACT
        ResultadoSimulacion resultado = servicio.simular(capital, aporte, tiempo);

        // ASSERT — va de año 0 hasta año N, por eso N+1 elementos
        assertThat(resultado.getProyeccionConservador()).hasSize(tiempo + 1);
        assertThat(resultado.getProyeccionModerado()).hasSize(tiempo + 1);
        assertThat(resultado.getProyeccionAgresivo()).hasSize(tiempo + 1);
    }

    @Test
    void simular_debeRetornarCapitalInicialEnAnioCero() {
        // ARRANGE
        long capital = 10_000;
        long aporte = 500;
        int tiempo = 5;

        // ACT
        ResultadoSimulacion resultado = servicio.simular(capital, aporte, tiempo);

        // ASSERT — en año 0 no hay crecimiento, proyectado debe ser igual al capital
        ProyeccionAnual anio0 = resultado.getProyeccionConservador().get(0);
        assertThat(anio0.getProyectado()).isEqualTo(capital);
    }

    @Test
    void simular_debeCalcularValorExacto_porPerfil() {
        // ARRANGE — aporte=0 simplifica la fórmula a: capital × (1 + tasa/12)^(años×12)
        long capital = 10_000;
        long aporte = 0;
        int tiempo = 1;

        // ACT
        ResultadoSimulacion resultado = servicio.simular(capital, aporte, tiempo);

        // ASSERT
        assertThat(resultado.getConservador().getValorFuturo()).isEqualTo(10_304L);
        assertThat(resultado.getModerado().getValorFuturo()).isEqualTo(10_723L);
        assertThat(resultado.getAgresivo().getValorFuturo()).isEqualTo(11_157L);
    }
}
