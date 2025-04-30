package org.example.rabbits;

import org.example.RunConfig;
import org.example.dataBase.entities.DbRabbit;
import org.example.thread.SortableData;
import org.example.thread.VeryImportantSort;

import static java.lang.Math.abs;
import static org.example.thread.ThreadBenchmarkData.benchmark;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


public class Zootopia {
    private ArrayList<Rabbit> fluffle; // fluffle = group of rabbits
    private final Supplier<Set<Rabbit>> dataStructureType;
    private VeryImportantSort veryImportantSort;

    public Zootopia() {
        String[] args = new String[0];
        RunConfig config = new RunConfig(args);
        this.fluffle = null;
        this.dataStructureType = config.getDataStructure();
        this.veryImportantSort = new VeryImportantSort(config.getSeed(), config.getInputSizeList().get(0)
                ,config.getThreadCountList().get(0));
    }

    public Zootopia(Supplier<Set<Rabbit>> dataStructureType) {
        this.fluffle = null;
        this.dataStructureType = dataStructureType;
        this.veryImportantSort = null;
    }

    public Zootopia(ArrayList<Rabbit> fluffle, Supplier<Set<Rabbit>> dataStructureType) {
        this.fluffle = fluffle;
        this.dataStructureType = dataStructureType;
    }

    public Zootopia(Supplier<Set<Rabbit>> dataStructureType, int arrayToBeSortedSize, int threadNum, long seed) {
        this.fluffle = null;
        this.dataStructureType = dataStructureType;
        this.veryImportantSort = new VeryImportantSort(seed, arrayToBeSortedSize,threadNum);
    }

    public Zootopia(RunConfig config) {
        this.fluffle = null;
        this.dataStructureType = config.getDataStructure();
        this.veryImportantSort = new VeryImportantSort(config.getSeed(), config.getInputSizeList().get(0)
                ,config.getThreadCountList().get(0));
    }

    public void modifyVeryImportantSort(int arrayToBeSortedSize, int threadNum, long seed) {
        this.veryImportantSort = new VeryImportantSort(seed, arrayToBeSortedSize,threadNum);
    }

    public void modifyVeryImportantSort(ArrayList<SortableData> arrayToBeSorted, int threadNum) {
        this.veryImportantSort = new VeryImportantSort(arrayToBeSorted,threadNum);
    }


    public void modifyVeryImportantSort(RunConfig config, int index) {
        this.veryImportantSort = new VeryImportantSort(config.getSeed(), config.getInputSizeList().get(index)
                ,config.getThreadCountList().get(index));
    }

    public List<DbRabbit> getDbFluffle() {
        List<DbRabbit> dbFluffle = new ArrayList<>();
        for(Rabbit family : fluffle) {
            DbRabbit dbFamily = family.intoDbRabbit(null);
            dbFluffle.add(dbFamily);
        }
        return dbFluffle;
    }

    public ArrayList<Rabbit> getFluffle(){
        return this.fluffle;
    }
    // add Rabbit to end of array
    public void pushRabbit(Rabbit rabbit) {
        this.fluffle.add(rabbit);
    }

    public void pushRabbit(String name,int age, float weight) {
        this.fluffle.add(new Rabbit(name, age, weight, dataStructureType));
    }


    public void initFluffleFromFile(String fileName) {
        Rabbit.resetId();
        ArrayList<Rabbit> rabbitVector = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            while((line=br.readLine())!= null) {
                //format: *rabbit_id* *name* *age* *weight* *parent_id*
                //ids: start form 1, and increase by one
                String[] parts = line.split(" ");
                String name = parts[1];
                int age = Integer.parseInt(parts[2]);
                float weight = Float.parseFloat(parts[3]);
                int parentId = Integer.parseInt(parts[4]);

                Rabbit rabbitToAdd = new Rabbit(name, age, weight, dataStructureType);

                // if rabbit is not a child, we add it to end of array
                if (parentId == -1) {
                    rabbitVector.add(rabbitToAdd);
                    continue;
                }

                // We search for parent of current rabbitToAdd
                for (Rabbit rabbitMember: rabbitVector) {
                    Rabbit parent = rabbitMember.findRabbit(parentId);

                    if (parent == null)
                        continue;

                    if (!parent.addChildren(rabbitToAdd))
                        System.out.println("Could not add: " + rabbitToAdd + " to: \n" + parent);

                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.fluffle = rabbitVector;
    }


    public static void generateBalancedFluffle(String fileName, int approxRabbitCount, int families, int childrenNodes) {
        // balanced tree size with 2 children each will be 1 + 2 + 4 + 8 + 16 + ...
        // balanced tree size with 3 children each will be 1 + 3 + 9 + 27 + ...
        // so it's childrenNodes^k - sum(childrenNodes^(k - 1) + childrenNodes^(k - 2) + ...)
        // ---> sum of geometric series

        int familySize = approxRabbitCount / families;
        // won't be perfectly balanced
        // familySize = roundToClosestBalance(familySize, childrenNodes);

        // so not to exceed queue max capacity
        if (familySize * families > 10000) {
            System.out.println("Too many rabbits, change families or rabbit count");
            return;
        }

        try {
            FileWriter myWriter = new FileWriter(fileName);
            AtomicInteger rabbitId = new AtomicInteger(1);
            Random rand = new Random();

            Queue<String> q = new LinkedList<>();
            for (int i = 0; i < families; i++) {
                int rootId = rabbitId.getAndIncrement();
                int rootAge = abs(rand.nextInt()) % 10;
                float rootWeight = rand.nextFloat();

                q.add("%d DbRabbit%d %d %f -1".formatted(rootId, rootId, rootAge, rootWeight));

                while(rabbitId.get() < (i + 1) * familySize) {
                    String currentRabbit =  q.remove();
                    myWriter.write(currentRabbit + "\n");

                    // we need first part of the string for the parentID for the children
                    String[] tokens = currentRabbit.split(" ");
                    int parentId = Integer.parseInt(tokens[0]);

                    // generate childrenNodes amount of children with the same parentId
                    for (int j = 0; j < childrenNodes; j++) {
                        int childId = rabbitId.getAndIncrement();
                        int childAge = abs(rand.nextInt()) % 10;
                        float childWeight = rand.nextFloat();
                        q.add("%d DbRabbit%d %d %f %d".formatted(childId, childId, childAge, childWeight, parentId));

                        // if current family size exceeded
                        if (rabbitId.get() >= (i + 1) * familySize)
                            break;
                    }
                }

                while(!q.isEmpty()) {
                    String currentRabbit =  q.remove();
                    myWriter.write(currentRabbit + "\n");
                }
            }

            // Ofc we need to manually close the file because if not the buffer won't be flushed ;)
            myWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static int roundToClosestBalance(int number, int base) {
        int sum = 0;
        int currentPower = 1;

        // Add powers of the base until it exceeds the number
        while (sum + currentPower < number) {
            sum += currentPower;
            currentPower *= base;
        }

        return sum + currentPower;
    }


    public void benchmarkInitSort() {
        benchmark(() -> veryImportantSort.startSort(fluffle), veryImportantSort.getThreadNum(),veryImportantSort.getArraySize());
    }


    public void initSort() {
        veryImportantSort.startSort(fluffle);
    }
}
