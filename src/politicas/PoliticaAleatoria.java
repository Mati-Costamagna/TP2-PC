package main.politicas;
import java.util.ArrayList;
import java.util.Random;


public class PoliticaAleatoria implements PoliticaInterface {
    private final Random rand = new Random();
    private final int[] conflictos ={2,5,7};

    @Override
    public int elegirTransicion(boolean[] t) {
        ArrayList<Integer> transiciones = new ArrayList<>();
        for(int indice: conflictos) {
            if(t[indice]) {
                transiciones.add(indice);
            }
        }
        if(!transiciones.isEmpty()) {
            return transiciones.get(rand.nextInt(transiciones.size()));
        }
        else {
            for (int i = 0; i < t.length; i++) {
                if (t[i]) transiciones.add(i);
            }
            if (!transiciones.isEmpty()) {
                return transiciones.get(rand.nextInt(transiciones.size()));
            }
            return 0;
        }
    }
}
