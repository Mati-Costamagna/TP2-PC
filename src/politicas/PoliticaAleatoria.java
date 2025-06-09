package politicas;

import java.util.Random;
import politicas.Politica;

public class PoliticaAleatoria implements Politica {
    private final Random random = new Random();

    @Override
    public int elegirTransicion(int[] transiciones) {
        if (transiciones.length == 0) {
            throw new IllegalArgumentException("No hay transiciones disponibles.");
        }
        return transiciones[random.nextInt(transiciones.length)];
    }
}
