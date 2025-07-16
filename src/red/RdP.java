package main.red;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;

    public RdP(int[][] matrizI, int[] marcadoInicial) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia = matrizI;
        transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();
    }

    private synchronized void setTransicionesSensibilizadas() {
        for (int i = 0; i < this.matrizIncidencia.length; i++) { //columnas
            for (int j = 0; j < this.matrizIncidencia[i].length; j++) { //filas
                if (matrizIncidencia[j][i] == -1) {
                    if (marcado[j] < 1) {
                        transicionesSensibilizadas[i] = false;
                        break;
                    }
                    transicionesSensibilizadas[i] = true;
                }
            }
        }
    }

    private boolean invariantesPlaza() {
        if ((marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) != 3) {
            System.out.println("Invariante 1");
            return false;
        } else if (((marcado[1] + marcado[2]) != 1)) {
            System.out.println("Invariante 2");
            return false;
        } else if ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) != 1){
            System.out.println("Invariante 3");
            return false;
        }
        return (marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8] + marcado[9] + marcado[10] + marcado[11]) == 3 &&
                ((marcado[1] + marcado[2]) == 1) &&
                ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9] + marcado[10]) == 1);
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
    }

    public boolean disparar(int t) {
        if(transicionesSensibilizadas[t]) {
            int[] marcadoAnterior = marcado.clone();

            for (int i = 0; i < this.matrizIncidencia.length; i++) {
                marcado[i] = marcado[i] + matrizIncidencia[i][t];
            }
            if (!invariantesPlaza()) {
                marcado = marcadoAnterior.clone(); // Revertir el marcado si no se cumple la invariante de plaza
                System.out.println("No se cumple la invariante de plaza, revirtiendo el marcado.");
                return false; // No se cumple la invariante de plaza, no se dispara la transición
            }else {
                setTransicionesSensibilizadas();
                return true;
            }
        }else{
            return false; // La transición no está sensibilizada, no se dispara
        }

    }
}
