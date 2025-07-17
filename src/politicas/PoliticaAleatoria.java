package main.politicas;
import java.util.Random;


public class PoliticaAleatoria implements PoliticaInterface {
    private final Random rand = new Random();
    private final int[] conflictos = {2, 5, 7};

    @Override
    public int elegirTransicion(boolean[] t) {
        if (t[2] || t[5] || t[7]) {
            int a = rand.nextInt(conflictos.length);
            while (!t[conflictos[a]]) {
                a = rand.nextInt(conflictos.length);
            }
            return conflictos[a];
        } else {
            int a = rand.nextInt(t.length);
            while (!t[a]) {
                a = rand.nextInt(t.length);
            }
            return a;
        }
    }
}
