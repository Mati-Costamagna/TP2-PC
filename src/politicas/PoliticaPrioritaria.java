package main.politicas;

public class PoliticaPrioritaria implements PoliticaInterface {

    public PoliticaPrioritaria() {}
    @Override
    public  int elegirTransicion(boolean[] t){
        if(t[5]) {
            return 5;
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
