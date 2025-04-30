package org.example.thread.tuples;

import java.util.Objects;

public class StartEndOffsetTuple {
    public int startOffset;
    public int endOffset;
    //indexes that corresponds to first and last element that have to be sorted, BOTH should be included in soring
    public StartEndOffsetTuple(int start,int end) {
        startOffset = start;
        endOffset = end;
    }
    public  StartEndOffsetTuple(StartEndOffsetTuple tuple) {
        startOffset = tuple.startOffset;
        endOffset = tuple.endOffset;
    }
    public void addToBoth (int value) {
        startOffset += value;
        endOffset += value;
    }
    public boolean isValid() {
        if (startOffset <= endOffset) {
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        return "(%d , %d)".formatted(startOffset,endOffset);
    }

    public int difference() {
        return endOffset - startOffset;
    }
    public boolean equals(StartEndOffsetTuple  o) {
        if (this.startOffset != o.startOffset) {
            return false;
        }
        if (this.endOffset != o.endOffset) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StartEndOffsetTuple otherTuple = (StartEndOffsetTuple) obj;
        return this.endOffset == otherTuple.endOffset && this.startOffset == otherTuple.startOffset;
    }


    // Override hashCode() for consistent hashing
    @Override
    public int hashCode() {
        return Objects.hash(startOffset,endOffset);
    }
}
