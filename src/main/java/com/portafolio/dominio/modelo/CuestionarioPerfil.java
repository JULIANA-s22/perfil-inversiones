package com.portafolio.dominio.modelo;

import java.util.List;

public class CuestionarioPerfil {

    public static final List<String> PREGUNTAS = List.of(
            "Antes de comenzar, cuéntame: ¿conoces la diferencia entre un inversor conservador, uno moderado y uno agresivo? No hay respuesta incorrecta, ¡queremos entenderte mejor!",
            "¿Con cuánto capital en pesos colombianos cuentas para empezar a invertir?",
            "¿Qué porcentaje aproximado de tus ahorros totales representa este dinero? Esto nos ayuda a entender qué tan importante es para ti y qué nivel de riesgo puedes asumir.",
            "¿Cuántos años tienes y en cuánto tiempo aproximado esperas usar este dinero? ¿En menos de 3 años, entre 3 y 10, o estás pensando en tu retiro a largo plazo?",
            "¿Para qué es este dinero? Por ejemplo, ¿estás ahorrando para tu retiro, para comprar casa, educación, o simplemente para hacer crecer tu patrimonio?",
            "¿Cuánto puedes ahorrar o aportar mensualmente en pesos colombianos para seguir haciendo crecer tu inversión?",
            "Si tu inversión bajara un 20% de valor temporalmente, ¿cómo reaccionarías? ¿La dejarías quieta esperando que se recupere, retirarías parte del dinero, o te generaría mucha angustia?"
    );

    private CuestionarioPerfil() {}
}
