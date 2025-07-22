package main.estadisticas;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContadorTransiciones {
    private final Map<String, AtomicInteger> transicionCounts = new HashMap<>();

    public ContadorTransiciones() {
        for (int i = 0; i <= 11; i++) {
            transicionCounts.put(String.valueOf(i), new AtomicInteger(0));
        }
    }

    public void incrementar(String transicion) {
        transicionCounts.get(transicion).incrementAndGet();
    }

    public int get(String transicion) {
        return transicionCounts.get(transicion).get();
    }

    public int getMinCount(List<String> transiciones) {
        int minimo = Integer.MAX_VALUE;

        for (String transicion : transiciones) {
            int cantidad = get(transicion); // Obtener cuántas veces se disparó esta transición
            if (cantidad < minimo) {
                minimo = cantidad; // Actualizar el mínimo si encontramos una transición con menor cantidad
            }
        }

        // Si no se encontró ningún valor menor, devolver 0 como valor por defecto
        if (minimo == Integer.MAX_VALUE) {
            return 0;
        }else{
            return minimo;
        }
    }

    public Map<String, Integer> snapshot() {
        Map<String, Integer> copia = new HashMap<>();

        for (Map.Entry<String, AtomicInteger> entrada : transicionCounts.entrySet()) {
            String clave = entrada.getKey();         // Ej: "2", "5", etc.
            int valorActual = entrada.getValue().get(); // Cantidad actual de disparos

            copia.put(clave, valorActual); // Guardar ese valor en la copia
        }

        return copia;
    }
}