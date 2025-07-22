package main.politicas;
import java.util.ArrayList;
import java.util.Random;


public class PoliticaAleatoria implements PoliticaInterface {
    private final Random rand = new Random();
    private final int[] conflictos ={2,5,7};

    @Override
    public int elegirTransicion(boolean[] t) {
        ArrayList<Integer> candidatas = new ArrayList<>();

        // Primero buscamos en las transiciones conflictivas
        for (int c : conflictos) {
            if (c < t.length && t[c]) {
                candidatas.add(c);
            }
        }

        // Si hay candidatas entre los conflictos, elegimos de ahí
        if (!candidatas.isEmpty()) {
            return candidatas.get(rand.nextInt(candidatas.size()));
        }

        // Si no, buscamos cualquier transición disponible
        for (int i = 0; i < t.length; i++) {
            if (t[i]) {
                candidatas.add(i);
            }
        }

        // Elegimos aleatoriamente entre todas las disponibles, o devolvemos -1 si no hay ninguna
        if (candidatas.isEmpty()) {
            return -1; // No hay ninguna transición disponible para disparar
        } else {
            int indiceAleatorio = rand.nextInt(candidatas.size()); // Elegimos una al azar entre las candidatas
            return candidatas.get(indiceAleatorio); // Devolvemos el índice de transición elegida
        }

    }
}
