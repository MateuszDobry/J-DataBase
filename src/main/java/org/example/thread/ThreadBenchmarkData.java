package org.example.thread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// TODO this class should collect all valuable information about VeryImportantSort sorting method
public class ThreadBenchmarkData {

    private static boolean isHeaderWritten = false;

    public static void benchmark(Runnable method, int threadNum, int arrSize) {
        StringBuilder logData = new StringBuilder();

        if (!isHeaderWritten) {
            appendSystemInfo(logData, arrSize);
            isHeaderWritten = true;
        }


        System.out.println("Threads : " + threadNum);



        // Uzyskujemy dostęp do systemowego bean'a CPU
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        final double[] maxCpuUsage = {0};

        //final double[] maxCpuUsage = {0};  // Tablica do przechowywania maksymalnego zużycia CPU

        // Uruchamiamy wątek monitorujący zużycie CPU
        Thread monitorThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) {
                double currentCpuUsage = osBean.getSystemCpuLoad() * 100;
                synchronized (maxCpuUsage) {
                    // Zapisujemy tylko maksymalne zużycie
                    if (currentCpuUsage > maxCpuUsage[0]) {
                        maxCpuUsage[0] = currentCpuUsage;
                    }
                }
                try {
                    Thread.sleep(100); // Uniknięcie nadmiernego obciążenia CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Rozpoczynamy monitorowanie w tle
        monitorThread.start();
        long start = System.currentTimeMillis();
        method.run();
        long end = System.currentTimeMillis();
        if (Globals.CLOSE_PROGRAM) {
            return;
        }

        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            logData.append("Monitoring thread interrupted: ").append(e.getMessage()).append("\n");
        }

        logData.append(String.format("Threads - %d : Execution Time - %d ms, CPU max usage: %.2f%%%n",
                threadNum, (end - start), maxCpuUsage[0]));

        saveToFile(logData.toString());
    }

    private static void appendSystemInfo(StringBuilder logData, int arrSize) {
        logData.append("Operating System: Windows\n");


        /*String cpuName = getCpuName();
        logData.append("CPU Name: ").append(cpuName).append("\n");*/

        int availableCores = Runtime.getRuntime().availableProcessors();
        logData.append("Available cores: ").append(availableCores).append("\n");

        logData.append("Size of array: ").append(arrSize).append("\n\n");
    }

   /* private static String getCpuName() {
        String cpuName = "Unknown";
        try {
            Process process = Runtime.getRuntime().exec("wmic cpu get name");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine(); // Pomiń nagłówek
            cpuName = reader.readLine().trim();
            process.waitFor();
            if (coreCount > 0) {
                cpuUsage = totalUsage / coreCount; // Średnie zużycie CPU na rdzeń
            }
        } catch (Exception e) {
            cpuName = "Error retrieving CPU name: " + e.getMessage();
        }
        return cpuName;
    }*/

    private static void saveToFile(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("information.txt", true))) {
            writer.write(data);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
