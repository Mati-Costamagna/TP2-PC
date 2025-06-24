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
        for (int i = 0; i < this.matrizIncidencia.length; i++) { //columnas
            for (int j = 0; j < this.matrizIncidencia[i].length; j++) { //filas
                if (matrizIncidencia[j][i] == -1){
                    if(marcado[j] < 1){
                        transicionesSensibilizadas[i] = false;
                        break;
                    }
                    transicionesSensibilizadas[i] = true;
                }
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
