package org.example.thread;

import org.example.thread.tuples.StartEndOffsetTuple;

public class SortedPartStatus {
    StartEndOffsetTuple tuple;
    boolean isSorted;

    public SortedPartStatus (StartEndOffsetTuple tuple) {
        this.tuple = tuple;
        isSorted = false;
    }

    @Override
    public String toString() {
        return tuple.toString() + " " + isSorted;
    }
}
