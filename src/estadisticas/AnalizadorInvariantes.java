package main.estadisticas;

import java.util.*;

public class AnalizadorInvariantes {
    private final ContadorTransiciones contador;

    private final List<String> modoSimple = List.of("5", "6");
    private final List<String> modoMedio = List.of("2", "3", "4");
    private final List<String> modoAlto  = List.of("7", "8", "9", "10");

    public AnalizadorInvariantes(ContadorTransiciones contador) {
        this.contador = contador;
    }

    public int cantidadSimple() { return contador.getMinCount(modoSimple); }
    public int cantidadMedia()  { return contador.getMinCount(modoMedio); }
    public int cantidadAlta()   { return contador.getMinCount(modoAlto); }

    public boolean relacionesValidas() {
        int t0 = contador.get("0");
        int t1 = contador.get("1");
        int t11 = contador.get("11");
        int sumaModos = cantidadSimple() + cantidadMedia() + cantidadAlta();
        return t0 >= t1 && t1 >= sumaModos && sumaModos >= t11;
    }

    public int totalInvariantes() {
        return cantidadSimple() + cantidadMedia() + cantidadAlta();
    }
}