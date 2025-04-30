package org.example;

import org.example.comparators.RabbitAlphabeticalComparator;
import org.example.comparators.RabbitIdComparator;
import org.example.rabbits.Rabbit;

import java.util.*;
import java.util.function.Supplier;


public class RunConfig  {

    enum SortingOption {
        NO_SORTING("no sorting"),
        NATURAL_SORTING("natural sorting"),
        ALTERNATIVE_ID_SORTING("alternative ID sorting"),
        ALTERNATIVE_ALPHABETICAL_SORTING("alternative alphabetical sorting");

        private final String description;

        SortingOption(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /* Zootopia part */
    private final ArrayList<Integer> threadCount;
    private final ArrayList<Integer> inputSize;
    private Supplier<Set<Rabbit>> dataStructure;
    private SortingOption sortingOption;


    /* Client-Server part */
    private int portNumber;

    /* IMPORTANT SEED TO THE PROGRAM */
    private final long seed = 0x1337;


    public RunConfig(String[] args) {
        this.threadCount = new ArrayList<>();
        this.inputSize = new ArrayList<>();
        this.setDataStructure(HashSet::new, SortingOption.NO_SORTING);
        this.portNumber = 2000;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-N" -> this.setDataStructure(TreeSet::new,
                        SortingOption.NATURAL_SORTING);
                case "-AB" -> this.setDataStructure(() -> new TreeSet<>(new RabbitAlphabeticalComparator()),
                        SortingOption.ALTERNATIVE_ALPHABETICAL_SORTING);
                case "-ID" -> this.setDataStructure(() -> new TreeSet<>(new RabbitIdComparator()),
                        SortingOption.ALTERNATIVE_ID_SORTING);
                // -th 100 2000 : 100 threads 2000 rabbit nodes
                case "-th" -> {
                    if (i + 2 >= args.length)
                        break;

                    try {
                        this.threadCount.add(Integer.parseInt(args[++i]));
                        this.inputSize.add(Integer.parseInt(args[++i]));
                    } catch (Exception e) {
                        throw new RuntimeException("RunConfig error", e);
                    }
                }
                case "-p" -> {
                    if (i + 1 >= args.length)
                        break;

                    try {
                        this.portNumber = Integer.parseInt(args[++i]);
                    } catch (Exception e) {
                        throw new RuntimeException("RunConfig error", e);
                    }
                }
                default -> System.out.println("Ignoring: " + args[i]);
            }
        }

        // if no -th met initialize to 1 thread arraySize 100000
        if (this.threadCount.isEmpty() || this.inputSize.isEmpty())
            this.initRun();
    }


    public ArrayList<Integer> getThreadCountList() {
        return this.threadCount;
    }

    public ArrayList<Integer> getInputSizeList() {
        return this.inputSize;
    }

    public Supplier<Set<Rabbit>> getDataStructure() {
        return this.dataStructure;
    }

    public long getSeed() {
        return this.seed;
    }

    public int getPortNumber() {
        return this.portNumber;
    }


    void initRun() {
        this.threadCount.add(1);
        this.inputSize.add(10);
    }

    public void setDataStructure(Supplier<Set<Rabbit>> ds, SortingOption option) {
        dataStructure = ds;
        sortingOption = option;
    }

    @Override
    public String toString() {
        return String.format("Sorting: %s, Iterations: %d",
                sortingOption, threadCount.size());
    }
}
