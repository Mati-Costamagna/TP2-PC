package main.red;

public class RdP {

    private int[] marcado;
    private final int[][] matrizIncidencia;
    private final boolean[] transicionesSensibilizadas;
    private final long[] tSensibilizadasConTiempo;
    private final long[] alpha;
    private final long[] beta;
    // private final long[] tiemposEspera;
    // private final boolean[] hilosEnEspera;

    public RdP(int[][] matrizI, int[] marcadoInicial, long[] alpha, long[] beta) {
        this.marcado = marcadoInicial;
        this.matrizIncidencia = matrizI;
        this.alpha = alpha;
        this.beta = beta;
        this.tSensibilizadasConTiempo = new long[matrizI.length];
        transicionesSensibilizadas = new boolean[matrizI.length];
        setTransicionesSensibilizadas();

    }

    private synchronized void setTransicionesSensibilizadas() {

        for (int i = 0; i < this.matrizIncidencia.length; i++) { // columnas
            for (int j = 0; j < this.matrizIncidencia[i].length; j++) { // filas
                if (matrizIncidencia[j][i] == -1) {
                    if (marcado[j] < 1) {
                        transicionesSensibilizadas[i] = false;
                        tSensibilizadasConTiempo[i] = 0;
                        break;
                    }
                    // Si estaba sensibilizada, el tiempo tiene que ser el de antes. Sino guardo uno
                    // nuevo
                    tSensibilizadasConTiempo[i] = transicionesSensibilizadas[i] && tSensibilizadasConTiempo[i] != 0
                            ? tSensibilizadasConTiempo[i]
                            : System.currentTimeMillis();
                    transicionesSensibilizadas[i] = true;
                }
            }
        }
    }

    private boolean invariantesPlaza() {
        boolean invariante1 = (marcado[0] + marcado[1] + marcado[3] + marcado[4] + marcado[5] + marcado[7] + marcado[8]
                + marcado[9] + marcado[10] + marcado[11]) == 3;
        boolean invariante2 = ((marcado[1] + marcado[2]) == 1);
        boolean invariante3 = ((marcado[4] + marcado[5] + marcado[6] + marcado[7] + marcado[8] + marcado[9]
                + marcado[10]) == 1);
        if (!invariante1) {
            System.out.println("Invariante 1 no se cumple");
            return false;
        } else if (!invariante2) {
            System.out.println("Invariante 2 no se cumple");
            return false;
        } else if (!invariante3) {
            System.out.println("Invariante 3 no se cumple");
            return false;
        }
        return true;
    }

    public boolean[] getTransicionesSensibilizadas() {
        return transicionesSensibilizadas;
    }

    public boolean testVentanaTiempo(int t) {
        return System.currentTimeMillis() - this.tSensibilizadasConTiempo[t] >= alpha[t];
    }

    public long getTimeToWait(int t) {
        long tiempoTranscurrido = System.currentTimeMillis() - this.tSensibilizadasConTiempo[t];
        long tiempoRestante = alpha[t] - tiempoTranscurrido;
        return Math.max(0, tiempoRestante);
    }

    public boolean disparar(int t) {
        int[] marcadoAnterior = marcado.clone();
        if (testVentanaTiempo(t)) {
            for (int i = 0; i < this.matrizIncidencia.length; i++) {
                marcado[i] = marcado[i] + matrizIncidencia[i][t];
            }
            if (!invariantesPlaza()) {
                marcado = marcadoAnterior.clone();
                System.out.println("No se cumple la invariante de plaza, revirtiendo el marcado.");
                return false;
            }
            setTransicionesSensibilizadas();
            return true;
        } else {
            return false;
        }
    }
}
