package main.threads;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger extends Thread {
    // Flag to signal the logger thread to stop processing new items
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    // Queue to hold incoming transition log messages
    private final BlockingQueue<String> transiciones = new LinkedBlockingQueue<>();
    private BufferedWriter writer;

    // Sequences to specifically monitor, as requested by the user.
    // Only these sequences will be verified and counted.
    private final List<String> secuencia5_6 = Arrays.asList("5", "6");
    private final List<String> secuencia2_3_4 = Arrays.asList("2", "3", "4");
    private final List<String> secuencia7_8_9_10 = Arrays.asList("7", "8", "9", "10");


    // Counters for the occurrences of the specified sequences
    private int countSecuencia5_6 = 0;
    private int countSecuencia2_3_4 = 0;
    private int countSecuencia7_8_9_10 = 0;


    public Logger() {
        try {
            // Initialize BufferedWriter to write logs to a file
            this.writer = new BufferedWriter(new FileWriter("log_estadisticas.txt"));
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs a transition by adding its index to the queue.
     * @param transicion The integer index of the transition fired.
     */
    public void logTransicion(int transicion) {
        if (!shouldStop.get()) { // Only log if not explicitly told to stop
            transiciones.offer(String.valueOf(transicion)); // Non-blocking add to the queue, convert int to String
        }
    }

    /**
     * Signals the logger thread to stop processing new messages.
     * It also interrupts the thread to unblock it if it's currently waiting on the queue.
     */
    public void signalStop() {
        shouldStop.set(true);
        this.interrupt(); // Interrupt to unblock from transiciones.take() if it's waiting
    }

    /**
     * Checks if the total count of the *new* specific sequences has reached the maximum threshold (200).
     * This method is used by other threads (e.g., Transiciones) to decide when to stop.
     * @return true if the total count is 200 or more, false otherwise.
     */
    public boolean hasReachedMaxInvariants() {
        // The stopping condition now considers the sum of the new sequence counts.
        return (countSecuencia5_6 + countSecuencia2_3_4 + countSecuencia7_8_9_10) >= 200;
    }

    @Override
    public void run() {
        // The buffer size should be large enough to accommodate the longest sequence we are checking.
        // The longest new sequence is secuencia7_8_9_10 with length 4.
        LinkedList<String> buffer = new LinkedList<>(); // Sliding window buffer for invariant checks
        try {
            // Loop as long as we haven't reached the max invariant count AND
            // (we are not explicitly told to stop OR there are still items in the queue to process).
            // This ensures all buffered items are processed before exiting after a stop signal.
            while (!hasReachedMaxInvariants() && (!shouldStop.get() || !transiciones.isEmpty())) {
                String transicion;
                try {
                    if (shouldStop.get()) {
                        // If stopping, use poll() to retrieve existing items without blocking
                        transicion = transiciones.poll();
                        if (transicion == null) {
                            // If queue is empty and we should stop, break the loop
                            break;
                        }
                    } else {
                        // Otherwise, block until a new transition message arrives
                        transicion = transiciones.take();
                    }
                } catch (InterruptedException e) {
                    // If interrupted, it's usually a signal to stop. Set flag and re-interrupt.
                    shouldStop.set(true);
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    continue; // Re-evaluate loop condition to drain remaining queue items
                }

                // Write the transition to the log file
                writer.write(transicion + "\n");

                // Add to buffer and maintain sliding window size.
                // Max length of new sequences is 4 (secuencia7_8_9_10).
                buffer.add(transicion);
                if (buffer.size() > 4) {
                    buffer.removeFirst();
                }

                // Check for occurrences of the only desired sequences
                if (buffer.size() >= secuencia5_6.size()) {
                    if (buffer.subList(buffer.size() - secuencia5_6.size(), buffer.size()).equals(secuencia5_6)) {
                        countSecuencia5_6++;
                        System.out.println("Sequence {5,6} detected! Count: " + countSecuencia5_6);
                    }
                }
                if (buffer.size() >= secuencia2_3_4.size()) {
                    if (buffer.subList(buffer.size() - secuencia2_3_4.size(), buffer.size()).equals(secuencia2_3_4)) {
                        countSecuencia2_3_4++;
                        System.out.println("Sequence {2,3,4} detected! Count: " + countSecuencia2_3_4);
                    }
                }
                if (buffer.size() >= secuencia7_8_9_10.size()) {
                    if (buffer.subList(buffer.size() - secuencia7_8_9_10.size(), buffer.size()).equals(secuencia7_8_9_10)) {
                        countSecuencia7_8_9_10++;
                        System.out.println("Sequence {7,8,9,10} detected! Count: " + countSecuencia7_8_9_10);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ensure all remaining items in the queue are written to the file before closing
            while (!transiciones.isEmpty()) {
                try {
                    String remainingTrans = transiciones.poll();
                    if (remainingTrans != null) {
                        writer.write(remainingTrans + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error writing remaining logs: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Write final counts of only the desired sequences to the file and close the writer
            try {
                writer.write("--- Final Sequence Counts ---\n");
                writer.write("Secuencia {5,6}: " + countSecuencia5_6 + "\n");
                writer.write("Secuencia {2,3,4}: " + countSecuencia2_3_4 + "\n");
                writer.write("Secuencia {7,8,9,10}: " + countSecuencia7_8_9_10 + "\n");
                writer.close();
                System.out.println("Log file 'log_estadisticas.txt' closed. Final counts written.");
            } catch (IOException e) {
                System.err.println("Error closing log file or writing final counts: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
