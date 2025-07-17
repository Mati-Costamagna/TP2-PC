package main.politicas;

import java.util.Random;
public class PoliticaPrioritaria implements PoliticaInterface {

    public PoliticaPrioritaria() {
    }

    private final Random rand = new Random();

    @Override
    public int elegirTransicion(boolean[] t) {
        if (t[5]) {
            return 5;
        } else {
            int a = rand.nextInt(t.length);
            while (!t[a]) {
                a = rand.nextInt(t.length);
            }
            return a;
        }
    }
}