package util;

import java.util.Set;

public class ModoProcesamiento {
    private static final Set<Integer> transicionesSimples = Set.of(7); // T7
    private static final Set<Integer> transicionesMedias = Set.of(4, 5); // T4, T5
    private static final Set<Integer> transicionesAltas  = Set.of(8, 9, 10); // T8, T9, T10

    public static boolean esSimple(int t) {
        return transicionesSimples.contains(t);
    }

    public static boolean esMedia(int t) {
        return transicionesMedias.contains(t);
    }

    public static boolean esAlta(int t) {
        return transicionesAltas.contains(t);
    }
}
