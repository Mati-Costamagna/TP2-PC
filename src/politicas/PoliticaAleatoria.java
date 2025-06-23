package main.politicas;
import java.util.ArrayList;
import java.util.Random;


public class PoliticaAleatoria implements PoliticaInterface {
    private final Random rand = new Random();
    private final int[] conflictos ={2,5,7};

    @Override
    public int elegirTransicion(boolean[] t) {
        if(t[2] || t[5] || t[7]){
            return conflictos[rand.nextInt(3)];
        }
        else{
            for (int i=0; i<t.length; i++){
                if(t[i]){
                    return i;
                }
            }
            return -1;
        }
    }
}
