package main.red;

public class RdP {

    private final int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;


    public RdP(int[][] matrizI, int[] marcadoInicial) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia  = matrizI;
        transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();

    }

    private void setTransicionesSensibilizadas(){
        for (int i = 0; i < this.matrizIncidencia.length; i++) {
            for (int j = 0; j < this.matrizIncidencia[i].length; j++) {
                transicionesSensibilizadas[j] = (marcado[i] > 0) && (matrizIncidencia[i][j] == -1);
            }
        }
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
    }

    public boolean disparar(int t){
        if (transicionesSensibilizadas[t]){
            for (int i = 0; i < this.matrizIncidencia.length; i++) {
                marcado[i] = marcado[i] + matrizIncidencia[i][t];
            }
            setTransicionesSensibilizadas();
            return true;
        }
        return false;
    }
}
