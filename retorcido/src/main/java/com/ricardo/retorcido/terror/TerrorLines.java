package com.ricardo.retorcido.terror;

import net.minecraft.util.RandomSource;

/** Frases de Dani por categoria. */
public final class TerrorLines {
    private TerrorLines() {}

    /** Dudas suaves (Fase 1+, y mood 1). */
    public static final String[] UNEASY = {
            "¿Escuchaste eso?",
            "Creí oír pasos...",
            "Qué raro, antes había una antorcha ahí",
            "¿Has oído algo?",
            "Tengo la sensación de que nos siguen.",
            "Hace mucho silencio... demasiado.",
            "Juraría que alguien nos mira.",
            "Qué frío de repente, ¿no?",
            "Me ha parecido ver algo moverse.",
            "No me gusta este sitio.",
    };

    /** Frases siniestras (Fase 3+). */
    public static final String[] SINISTER = {
            "Hay algo ahí fuera.",
            "Me está mirando... ¿tú también lo ves?",
            "No mires atrás.",
            "Siempre hay uno más cerca cuando apago la luz",
            "Está más cerca que antes.",
            "No te quedes quieto.",
            "Lo oigo respirar.",
            "Quédate cerca de la luz, por favor.",
            "Ya viene. Lo sé.",
            "No se va. Nunca se va.",
    };

    /** Frases extrañas/ausentes (mood 2, tras el regreso). */
    public static final String[] STRANGE = {
            "No me acuerdo de nada. Solo oscuridad.",
            "Él me dejó volver.",
            "Hace frío donde estuve.",
            "¿Cuánto tiempo estuve fuera?",
            "Todavía lo oigo llamarme.",
            "Ya no tengo miedo. Eso es lo raro.",
            "No me mires así. Estoy bien.",
            "Él dice que volverá por ti.",
            "Había muchos como yo allí.",
            "¿Sigues siendo tú?",
    };

    public static final String STARE_END = "...No es nada.";
    public static final String KIDNAP_START = "Oye... creo que oí algo por allá. Voy a ver, ya vuelvo.";
    public static final String RESPAWN = "Volví. Me caí en una cueva.";
    public static final String RETURN = "No me acuerdo de nada. Solo oscuridad.";
    public static final String HUNTER_KILLED = "¿Lo mataste...? Nunca vuelve del todo.";

    /** Elige una frase al azar distinta de {@code last}. */
    public static String pick(String[] list, String last, RandomSource rnd) {
        String s = list[rnd.nextInt(list.length)];
        for (int i = 0; i < 8 && s.equals(last) && list.length > 1; i++) {
            s = list[rnd.nextInt(list.length)];
        }
        return s;
    }
}
