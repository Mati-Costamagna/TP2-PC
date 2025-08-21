package main.threads;

import main.estadisticas.AnalizadorInvariantes;
import main.estadisticas.ContadorTransiciones;
import main.politicas.PoliticaInterface;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger extends Thread {
    private final AtomicBoolean finalizar = new AtomicBoolean(false);
    private final BlockingQueue<String> transiciones = new LinkedBlockingQueue<>();
    private BufferedWriter writer;
    private final String politica;
    private final long startTime;

    private final ContadorTransiciones contador = new ContadorTransiciones();
    private final AnalizadorInvariantes analizador = new AnalizadorInvariantes(contador);

    public Logger(PoliticaInterface p) {
        this.politica = p.getClass().getSimpleName();
        this.startTime = System.currentTimeMillis();
        try {
            this.writer = new BufferedWriter(new FileWriter("log_estadisticas.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logTransicion(int transicion) {
        if (!finalizar.get()) {
            transiciones.add(String.valueOf(transicion));
        }
    }

    public long getTotalExecutionTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    public void finalizarLogger() {
        finalizar.set(true);
        transiciones.add("TERMINAR");
    }

    public boolean isFinalizado() {
        return finalizar.get();
    }

    public boolean alcanzoCantMaxInvariantes() {
        return analizador.totalInvariantes() > 200;
    }

    private String obtenerSiguienteTransicion() {
        if(transiciones.isEmpty() && alcanzoCantMaxInvariantes()) {
            finalizarLogger();
            return null;
        }else {
            try {
                if (isFinalizado()) {
                    return transiciones.poll();
                } else {
                    return transiciones.take();
                }
            } catch (InterruptedException e) {
                System.out.println("Logger interrumpido: " + e.getMessage());
                return null;
            }
        }
    }

    @Override
    public void run() {
        try {
            while (!alcanzoCantMaxInvariantes() && (!isFinalizado() || !transiciones.isEmpty())) {
                String transicion = obtenerSiguienteTransicion();
                if (transicion == null || transicion.equals("TERMINAR")) break;

                writer.write(transicion + " ");
                contador.incrementar(transicion);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            // Guardar estadística al final como ya lo hacés
            try {
                writer.write("\n--- Política: " + politica + "\n");
                writer.write("--- Conteo por transición ---\n");
                contador.snapshot().forEach((k, v) -> {
                    try { writer.write("Transición " + k + ": " + v + "\n"); }
                    catch (IOException e) { e.printStackTrace(); }
                });
                writer.write("--- Conteo de invariantes ---\n");
                writer.write("Modo Simple: " + analizador.cantidadSimple() + "\n");
                writer.write("Modo Medio:  " + analizador.cantidadMedia() + "\n");
                writer.write("Modo Alto:   " + analizador.cantidadAlta() + "\n");
                writer.write("Relaciones válidas: " + (analizador.relacionesValidas() ? "✔️" : "❌") + "\n");
                writer.write("Tiempo total: " + getTotalExecutionTimeMillis() + " ms\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}