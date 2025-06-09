package util;

import java.util.HashMap;
import java.util.Map;

public class TransicionesTemporales {
    private static final Map<Integer, Integer> tiempos = new HashMap<>();

    static {
        // Transiciones T1, T3, T4, T6, T8, T9, T10
        tiempos.put(1, 100);
        tiempos.put(3, 150);
        tiempos.put(4, 120);
        tiempos.put(6, 200);
        tiempos.put(8, 180);
        tiempos.put(9, 220);
        tiempos.put(10, 250);
    }

    public static int getDelay(int transicion) {
        return tiempos.getOrDefault(transicion, 0);
    }
}
