package org.example;

import org.example.chart.BenchmarkChart;
import org.example.rabbits.Zootopia;
import org.example.thread.Globals;
import java.util.Locale;


public class Main {

    /* -ID -th 1 500000 -th 10 500000 -th 20 500000  -th 30 500000 -th 40 500000 */
    public static void lab23(String[] args) {
        Locale.setDefault(Locale.US);
        RunConfig config = new RunConfig(args);
        System.out.println(config);


        //Zootopia.generateBalancedFluffle("input-data/balanced-input.txt", 500, 5, 10);
        InputListener listener = new InputListener();
        listener.start();

        Zootopia.generateBalancedFluffle("input-data/balanced-input.txt", 9999, 5, 10);

        // we want to init zootopia once and then just modify existing class
        Zootopia zootopia = new Zootopia(config);
        zootopia.initFluffleFromFile("input-data/balanced-input.txt");
        zootopia.benchmarkInitSort();
        if (Globals.CLOSE_PROGRAM) {
            return;
        }
        for (int i = 1; i < config.getInputSizeList().size(); i++) {
            if (Globals.CLOSE_PROGRAM) {
                return;
            }
            zootopia.modifyVeryImportantSort(config, i);
            zootopia.benchmarkInitSort();
        }

        try {
            BenchmarkChart chart = new BenchmarkChart("Benchmark-Data");
            chart.pack();
            chart.setVisible(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public static void main(String[] args) {

    }
}