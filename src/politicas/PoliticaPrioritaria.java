package politicas;
import static util.ModoProcesamiento.esSimple;

public class PoliticaPrioritaria implements Politica {

    @Override
    public int elegirTransicion(int[] transiciones) {
        // Preferimos transiciones simples (por ejemplo, supongamos T7 es la simple)
        for (int t : transiciones) {
            if (esSimple(t)) return t;
        }
        // Si no hay simple, devolver la primera disponible
        return transiciones.length > 0 ? transiciones[0] : -1;
    }
}
