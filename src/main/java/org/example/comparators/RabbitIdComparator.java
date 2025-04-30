package org.example.comparators;

import org.example.rabbits.Rabbit;

import java.util.Comparator;

public class RabbitIdComparator implements Comparator<Rabbit> {
    @Override
    public int compare(Rabbit rab1, Rabbit rab2) {
        int idRab1 = rab1.getId();
        int idRab2 = rab2.getId();

        return Integer.compare(idRab1, idRab2);
    }
}
