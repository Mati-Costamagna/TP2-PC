package main.politicas;

import java.util.ArrayList;
import java.util.Random;

public class PoliticaPrioritaria implements PoliticaInterface {

    private final Random rand = new Random();

    @Override
    public int elegirTransicion(boolean[] transitions) {

        // Prioridad absoluta a la transición 5
        if (transitions[5]) {
            return 5;
        }

        // Listar todas las transiciones disponibles
        ArrayList<Integer> disponibles = new ArrayList<>();
        for (int i = 0; i < transitions.length; i++) {
            if (transitions[i]) {
                disponibles.add(i);
            }
        }

        // Elegir una aleatoria si hay disponibles; sino devolver -1
        if (disponibles.isEmpty()) {
            return -1; // No hay transiciones disponibles
        } else {
            int idx = rand.nextInt(disponibles.size());
            return disponibles.get(idx);
        }
    }
}