package org.example.thread.tuples;

public class StartEndOffsetMergeTuple {
    public int startOffset1;
    public int endOffset1;

    public int startOffset2;
    public int endOffset2;

    //indexes that corresponds to first and last element that have to be sorted, BOTH should be included in soring
    public StartEndOffsetMergeTuple(int start1,int end1,int start2,int end2) {
        startOffset1 = start1;
        endOffset1 = end1;
        startOffset2 = start2;
        endOffset2 = end2;
    }

    public  StartEndOffsetMergeTuple(StartEndOffsetTuple tuple1, StartEndOffsetTuple tuple2) {
        startOffset1 = tuple1.startOffset;
        endOffset1 = tuple1.endOffset;
        startOffset2 = tuple2.startOffset;
        endOffset2 = tuple2.endOffset;
    }

    public void addToBoth (int value) {
        startOffset1 += value;
        endOffset1 += value;
        startOffset2 += value;
        endOffset2 += value;
    }

    public boolean isValid(){
        if(startOffset1 <= endOffset1 && endOffset1 + 1 == startOffset2 && startOffset2 <= endOffset2){
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        return "(%d , %d , %d , %d )".formatted(startOffset1, endOffset1,startOffset2, endOffset2);
    }
}
