package org.example.comparators;

import org.example.rabbits.Rabbit;

import java.util.Comparator;

public class RabbitAlphabeticalComparator implements Comparator<Rabbit> {
    @Override
    public int compare(Rabbit rab1, Rabbit rab2) {
        String rab1Name = rab1.getName();
        String rab2Name = rab2.getName();

        return rab1Name.compareTo(rab2Name);
    }
}
