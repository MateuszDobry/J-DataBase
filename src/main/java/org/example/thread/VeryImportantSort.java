package org.example.thread;

import org.example.rabbits.Rabbit;
import org.example.thread.tuples.StartEndOffsetTuple;

import java.util.ArrayList;
import java.util.Random;


public class VeryImportantSort {
    private final ArrayList<SortableData> bigArray;
    private final SortingStations sortingStations;
    private final int threadNum;

    public VeryImportantSort(long seed, int bigArraySize, int threadNum) {
        Random rand = new Random(seed);

        bigArray = new ArrayList<>();
        for (int i = 0; i < bigArraySize; i++) {
            bigArray.add(new SortableData(rand));
        }
        sortingStations = new SortingStations(threadNum,bigArray);

        this.threadNum = threadNum;
    }

    public VeryImportantSort(ArrayList<SortableData> bigArray, int threadNum) {

        this.bigArray = bigArray;

        sortingStations = new SortingStations(threadNum,bigArray);

        this.threadNum = threadNum;
    }


    public int getThreadNum() {
        return this.threadNum;
    }


    // if the array is very big we should load it in portions
    public void loadPartOfArrayFromFile() {

    }

    // We might use pipelines
    public void pushArrayToPipeLine(){

    }

    public int getArraySize(){
        return this.bigArray.size();
    }

    public void startSort(ArrayList<Rabbit> fluffle) {
        // Distribute bigArray equally between all families

        int familyCount = fluffle.size();
        int portion = bigArray.size() / familyCount; // average number of elements, rounded down

        int startOffset = 0;
        int endOffset = portion - 1;

        for (int i = 0 ; i < (familyCount - 1); i++) { // ;last family is special
            StartEndOffsetTuple startEndOffset = new StartEndOffsetTuple(startOffset, endOffset);
            fluffle.get(i).handleSorting(startEndOffset, sortingStations);

            startOffset += portion;
            endOffset += portion;
        }

        StartEndOffsetTuple startEndOffset = new StartEndOffsetTuple(startOffset, bigArray.size() - 1); // last elements
        fluffle.get(familyCount - 1).handleSorting(startEndOffset, sortingStations);
        sortingStations.startAllThreads();
        sortingStations.endThreads();
        //printArray();

        if (!Globals.CLOSE_PROGRAM)
            testArray();
    }


    public void printArray() {
        int i = 0;
        for (SortableData data : bigArray) {
            System.out.println(i + "th -> " + data);
            i += 1;
        }
    }

    private void testArray() {
        System.out.println("Checking if merged properly...");
        for (int i = 1; i < bigArray.size(); i++) {
            if (!(bigArray.get(i).compareTo(bigArray.get(i - 1)) <= 0)) {
                System.out.println("Array is not sorted in descending order at index " + i
                                                    + "\n " + bigArray.get(i) + " <= " +  bigArray.get(i - 1) );
            }
        }
        System.out.println("Properly merged!!!");
    }

    private void printToLike(String fileName) {

    }

}
