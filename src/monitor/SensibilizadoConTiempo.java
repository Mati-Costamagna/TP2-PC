package main.monitor;

public class SensibilizadoConTiempo {
    private final boolean[] transicionesLevantadas;
    private final long[] hilosADormir;

    public SensibilizadoConTiempo(int size) {
        this.transicionesLevantadas = new boolean[size];
        this.hilosADormir = new long[size];
    }

    public boolean estaLevantado(int t){
        return transicionesLevantadas[t];
    }

    public void dormir(int t){
        try{
            Thread.sleep(tiempoADormir(t));
            transicionesLevantadas[t] = true;
        }catch (InterruptedException e){
            System.out.println("Hilo interrumpido al intentar dormir");
        }
    }

    public boolean hayAlguienLevantado(){
        for (int i = 0; i < transicionesLevantadas.length; i++) {
            if(transicionesLevantadas[i]){
                return true;
            }
        }
        return false;
    }

    public void entroAlMonitor(int t){
        transicionesLevantadas[t] = false;
    }

    public void setDormir(int t, long tiempo){
        hilosADormir[t] = tiempo;
    }

    private long tiempoADormir(int t){
        return hilosADormir[t];
    }

    public boolean tieneQueDormir(int t){
        return tiempoADormir(t) > 0;
    }
}
