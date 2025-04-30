package org.example.dataBase.entities;

import java.util.Comparator;

public class DbCageRabbitCountComparator implements Comparator<DbCage> {

    @Override
    public int compare(DbCage o1, DbCage o2) {
        return Integer.compare(o2.getRabbits().size(), o1.getRabbits().size());
    }
}
