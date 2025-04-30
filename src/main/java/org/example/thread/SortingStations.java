package org.example.thread;



// this class represents amount of threads our program will have.
// We will not be creating new thread when some DbRabbit start to sort,
// but it should this class.

import org.example.thread.status_enums.SortStatus;
import org.example.thread.status_enums.ThreadStatus;
import org.example.thread.tuples.StartEndOffsetMergeTuple;
import org.example.thread.tuples.StartEndOffsetTuple;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

// TODO implement this object handling our threads
public class SortingStations {
    ExecutorService executor; // thread manager
    ArrayList<Thread> stationArr; // thread array
    Vector<ThreadStatus> threadStatusArr; // status array fo threads
    int threadNum;

    BlockingQueue<StartEndOffsetTuple> waitingSortList; // tuple (startOffset1, endOffset1) which describes part of array to be sorted
    BlockingQueue<StartEndOffsetMergeTuple> waitingMergeList; // 1 tuple (start1, end1, start2, end2) merge data for thread
    ConcurrentHashMap<StartEndOffsetTuple, SortStatus> sortStatus; // array containing information about status if the part of array is sorted (required for merge)
    LinkedList<StartEndOffsetTuple> keysToTuples;

    //ArrayList<SortableData> sortedArray;


    public SortingStations(int threadNum, ArrayList<SortableData> wholeArray) {
        executor = Executors.newFixedThreadPool(threadNum);
        stationArr = new ArrayList<Thread>(threadNum);
        this.threadNum = threadNum;

        this.waitingSortList = new ArrayBlockingQueue<StartEndOffsetTuple>(10000);
        this.waitingMergeList = new ArrayBlockingQueue<StartEndOffsetMergeTuple>(10000);
        this.threadStatusArr = new Vector<ThreadStatus>();
        this.sortStatus = new ConcurrentHashMap<>();
        this.keysToTuples = new LinkedList<>();

        for (int i = 0; i<threadNum ;i++) {
            stationArr.add(new Thread(new SingleSortingStation(i, waitingSortList,
                            waitingMergeList, wholeArray, threadStatusArr, sortStatus
            )));
            threadStatusArr.add(ThreadStatus.FREE);
        }
    }


    private void printSortStatus() {
        System.out.println("Sort status:");
        for (StartEndOffsetTuple key : keysToTuples) {
            System.out.println(key + " -> " + sortStatus.get(key));
        }
    }

    private void loadThreadStatus() {
        // this list should be sorted from start
        // end of one tuple is start of next one
        for(StartEndOffsetTuple tuple: waitingSortList) {
            sortStatus.put(tuple, SortStatus.TO_BE_SORTED);
        }
    }

    private void waitForThreads(int checkoutTime) {
        // waiting for all threads to end
        boolean allThreadEnded = false;
        while (!allThreadEnded) {
            allThreadEnded = threadStatusArr.stream().allMatch(t -> t == ThreadStatus.FREE);
            try {
                TimeUnit.MILLISECONDS.sleep(checkoutTime);
            } catch (Exception e) {
                System.out.println();
            }
        }
    }

    // remembering all keys to hashmap, keys should be sorted
    private void loadTuples() {
        keysToTuples.addAll(waitingSortList);
    }
    private void makeThreadsSort() {
        System.out.println("Sorting...");
        int sortElementsCount = waitingSortList.size();
        int i = 0;
        while (sortElementsCount != 0 && !Globals.CLOSE_PROGRAM) {
            if (threadStatusArr.get(i) == ThreadStatus.FREE) {
                threadStatusArr.set(i,ThreadStatus.SORTING);
                executor.execute(stationArr.get(i));
                sortElementsCount--;
            }
            i++;
            i %= threadNum;
        }
    }

    public void generatePairsToMerge() {
        for (int i = 0; i < keysToTuples.size() -1; i++) {
            // key = tuple (startOffset, endOffset)
            StartEndOffsetTuple key1 = keysToTuples.get(i);
            StartEndOffsetTuple key2 = keysToTuples.get(i+1);
            if ((sortStatus.get(key1) != SortStatus.MERGED_SUCCESSFULLY &&
                    sortStatus.get(key2) != SortStatus.MERGED_SUCCESSFULLY)) {
                continue;
            }

            // if 2 neighbour pairs are sorted, we can merge them
            StartEndOffsetMergeTuple mergeTuple = new StartEndOffsetMergeTuple(key1,key2);
            if(!mergeTuple.isValid()){
                System.out.println("Hay that's illegal");
            }
            // thread get: (start1, end1, start2,end2)
            // thread will set key : (start1, end2)
            waitingMergeList.add(mergeTuple);

            // creating key: (start1, end2)
            // deleting old pairs
            sortStatus.remove(key1);
            sortStatus.remove(key2);

            // adding new key: (start1, end2)
            StartEndOffsetTuple newKey = new StartEndOffsetTuple(key1.startOffset, key2.endOffset);
            sortStatus.put(newKey, SortStatus.TO_BE_MERGED);

            // updating keysToTuples
            keysToTuples.remove(i+1); // removing second one
            // extend the updating first element
            keysToTuples.set(i, newKey);
        }
    }
    public void executeThreadMerge() {
        int mergeElements = waitingMergeList.size();
        int i = 0;
        while (mergeElements != 0 && !Globals.CLOSE_PROGRAM) {
            // finding free thread to give task
            if (threadStatusArr.get(i) == ThreadStatus.FREE) {
                threadStatusArr.set(i,ThreadStatus.MERGING);
                executor.execute(stationArr.get(i));
                mergeElements--;
            }
            i++;
            i %= threadNum;
        }
    }

    public void makeThreadsMerge() {
        System.out.println("Merging...");
        while(sortStatus.size() != 1 && !Globals.CLOSE_PROGRAM) {
            generatePairsToMerge();
            executeThreadMerge();
            waitForThreads(0);
        }

    }
    public void startAllThreads() {
        validateWaitingSortList();
        loadThreadStatus();
        loadTuples();
        makeThreadsSort();
        waitForThreads(0);
        makeThreadsMerge();

        waitForThreads(0);
        if (!Globals.CLOSE_PROGRAM) {
            System.out.println("All merged!");
        }
        else {
            System.out.println("Sort Aborted");
        }
    }
    public void validateWaitingSortList(){
        BlockingQueue<StartEndOffsetTuple> newSortList = new ArrayBlockingQueue<StartEndOffsetTuple>(10000);
        Vector<StartEndOffsetTuple> sortArr = new Vector<StartEndOffsetTuple>(waitingSortList);
        if(waitingSortList.peek() != null)
            newSortList.add(waitingSortList.peek());
        for(int i = 1; i < sortArr.size() -1;i++) {
            StartEndOffsetTuple prevTuple = new StartEndOffsetTuple(sortArr.get(i-1));
            StartEndOffsetTuple curTuple = new StartEndOffsetTuple(sortArr.get(i));
            StartEndOffsetTuple nextTuple = new StartEndOffsetTuple(sortArr.get(i+1));
            if(curTuple.isValid()) {
                newSortList.add(curTuple);
                continue;
            }
            newSortList.add(new StartEndOffsetTuple(prevTuple.endOffset + 1, nextTuple.endOffset));
            i++;
        }
        newSortList.add(sortArr.get(sortArr.size() - 1));

        this.waitingSortList.clear();
        this.waitingSortList.addAll(newSortList);
    }

    public void addToSortingQueue(StartEndOffsetTuple startEndOffset) {
        waitingSortList.add(startEndOffset);
    }


    public void letThreadSort(Vector<SortableData> arraySlice) {
        stationArr.get(0);
    }

    public void endThreads() {
        executor.close();

    }
}
