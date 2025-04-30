package org.example.thread;

import java.io.Serializable;
import java.util.Random;

public class SortableData implements Comparable<SortableData>, Serializable {
    private static final long serialVersionUID = 1L; // Dodanie serialVersionUID

    int someNum;
    float someFloat;
    char someChar;

    public SortableData(Random rand) {
        someNum = rand.nextInt(1_000_000); // Upewnij się, że nie używasz % w tym przypadku
        someFloat = rand.nextFloat();
        someChar = (char) (rand.nextInt(127 - 32) + 32);
    }

    @Override
    public String toString() {
        return String.format("i: %d, f: %f, c: %c \n", someNum, someFloat, someChar);
    }

    @Override
    public int compareTo(SortableData o) {
        // Porównanie według someNum, someChar, someFloat
        int numComparison = Integer.compare(this.someNum, o.someNum);
        if (numComparison != 0) {
            return numComparison;
        }

        int charComparison = Character.compare(this.someChar, o.someChar);
        if (charComparison != 0) {
            return charComparison;
        }

        return Float.compare(this.someFloat, o.someFloat);
    }
}
