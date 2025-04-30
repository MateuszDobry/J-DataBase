package org.example.thread;

import org.example.thread.status_enums.SortStatus;
import org.example.thread.status_enums.ThreadStatus;
import org.example.thread.tuples.StartEndOffsetMergeTuple;
import org.example.thread.tuples.StartEndOffsetTuple;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

// TODO implement ✨insert sort✨, or just anything you want
public class SingleSortingStation implements  Runnable {
    int myIndex;
    BlockingQueue<StartEndOffsetTuple>  waitingList;
    ArrayList<SortableData> wholeArray;
    Vector<ThreadStatus> threadStatusArr;
    ConcurrentHashMap<StartEndOffsetTuple, SortStatus> sortStatus;

    BlockingQueue<StartEndOffsetMergeTuple> waitingMergeList;

    StartEndOffsetMergeTuple tupleData = null;

    public SingleSortingStation(int i, BlockingQueue<StartEndOffsetTuple>  waitingList,
                                BlockingQueue<StartEndOffsetMergeTuple> waitingMergeList,
                                ArrayList<SortableData> wholeArray,
                                Vector<ThreadStatus> threadStatusArr,
                                ConcurrentHashMap<StartEndOffsetTuple,
                                SortStatus> sortStatus
    ) {
        myIndex = i;
        this.waitingList = waitingList;
        this.waitingMergeList = waitingMergeList;
        this.wholeArray = wholeArray;
        this.threadStatusArr = threadStatusArr;
        this.sortStatus = sortStatus;
    }


    public Vector<SortableData> someFancyOperationOnArr(Vector<SortableData> array) {
        return null;
    }

    // left and right are offsets of border elements
    public void insertSort(int left, int right) {
        threadStatusArr.set(myIndex, ThreadStatus.SORTING);

        int size = right - left + 1;

        for(int i = 1; i < size && !Globals.CLOSE_PROGRAM; i++) {
            SortableData key = wholeArray.get(left + i);
            int j = i - 1; // j = all previous elements

            while( j >= 0 && key.compareTo(wholeArray.get(left+j)) > 0) {
                wholeArray.set(left + j + 1, wholeArray.get(left + j));
                j -= 1;
            }
            wholeArray.set(left + j + 1, key);
        }
    }

    private void sort() {
        StartEndOffsetTuple offsets = waitingList.remove();
        if (offsets == null) {
            System.out.println("Thread " + myIndex + " did not get the array to sort");
            return;
        }

        //System.out.println("T " + myIndex +" sorting " + offsets + "elements");
        insertSort(offsets.startOffset, offsets.endOffset);

        // indicating that the sort is over
        if (sortStatus.replace(offsets, SortStatus.MERGED_SUCCESSFULLY) == null) {
            System.out.println("T " + myIndex + " could not find my array in sorting");
        }
//        else {
//            System.out.println("T " + myIndex + " sorting complete" + offsets);
//        }

    }



    private void merge() {
        try {
            //System.out.println("T " + myIndex + " trying to remove from merge list");
            StartEndOffsetMergeTuple offsets = waitingMergeList.poll();
            if (offsets == null) {
                System.out.println("Thread " + myIndex + " did not get array to merge");
                return;
            }
            this.tupleData = offsets;

            //System.out.println("Thread " + myIndex + " merging " + offsets);
            int start1 = offsets.startOffset1;
            int start2 = offsets.startOffset2;


            int left1 = offsets.startOffset1;
            int right1 = offsets.endOffset1;
            int left2 = offsets.startOffset2;
            int right2 = offsets.endOffset2;

            ArrayList<SortableData> merged = new ArrayList<>();

            if (right2 - left2 < 0 || right1 - left1 < 0){
                System.out.println("ups");
            }
            while (left1 <= right1 && left2 <= right2) {
                SortableData data1 = wholeArray.get(left1);
                SortableData data2 = wholeArray.get(left2);

                int result = data1.compareTo(data2);
                if (result > 0) {
                    merged.add(data1);
                    left1++;
                } else if (result < 0) {
                    merged.add(data2);
                    left2++;
                } else {
                    merged.add(data1);
                    left1++;
                    merged.add(data2);
                    left2++;
                }
                if (Globals.CLOSE_PROGRAM) {
                    return;
                }
            }

            while (left1 <= right1) {
                merged.add(wholeArray.get(left1));
                left1++;
            }

            while (left2 <= right2){
                merged.add(wholeArray.get(left2));
                left2++;
            }

            //System.out.println("Checking if merged properly...");
            for (int i = 1; i < merged.size(); i++) {
                if (!(merged.get(i).compareTo(merged.get(i - 1)) <= 0)) {
                    System.out.println("Did not merger properly " + i
                            + "\n " + merged.get(i) + " <= " +  merged.get(i - 1) );
                }
            }



            // now we write our data to its place in array
            int j = 0;
            for (SortableData data : merged) {
                wholeArray.set(start1 + j, data);
                j++;
            }
            // indicating that the merge is over
            StartEndOffsetTuple key = new StartEndOffsetTuple(offsets.startOffset1, offsets.endOffset2);
            sortStatus.replace(key,SortStatus.MERGED_SUCCESSFULLY);

            if (sortStatus.replace(key,SortStatus.MERGED_SUCCESSFULLY) == null) {
                System.out.println("T " + myIndex + " could not find my array in merging");
            }
//             else {
//                System.out.println("T " + myIndex + " merging complete" + offsets);
//             }


        } catch (Exception e) {
            System.out.println("T " + myIndex + " failed to merge arrays ;cc -> " + e);
        }

    }

    @Override
    public void run() {
        try {
            //System.out.println("Thread " + myIndex + " is running");

            if (threadStatusArr.get(myIndex) == ThreadStatus.SORTING) {
                sort();
            } else if (threadStatusArr.get(myIndex) == ThreadStatus.MERGING) {
                merge();
            } else {
                System.out.println("Thread " + myIndex + " i should not be here");
            }
            threadStatusArr.set(myIndex, ThreadStatus.FREE);
        }
        catch (Exception e) {
            // Throwing an exception
            System.out.println("Thread " + myIndex + "  exception is caught " + e);
            threadStatusArr.set(myIndex, ThreadStatus.FREE);
        }
    }
}
