package org.example.unitTest;

import org.example.dataBase.entities.DbBuilding;
import org.example.dataBase.entities.DbCage;
import org.example.dataBase.entities.DbRabbit;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Repository
{
    // this is for giving rabbits id's - without DB id won't be generated automatically :(
    private final AtomicInteger rabbitIdTagger;
    private final AtomicInteger cageIdTagger;
    private final AtomicInteger buildingIdTagger;

    private final Map<Integer, DbRabbit> rabbitRecords;
    private final Map<Integer, DbCage> cageRecords;
    private final Map<Integer, DbBuilding> buildingRecords;


    public Repository() {
        this.rabbitIdTagger = new AtomicInteger(1);
        this.cageIdTagger = new AtomicInteger(1);
        this.buildingIdTagger = new AtomicInteger(1);

        this.rabbitRecords = new HashMap<>();
        this.cageRecords = new HashMap<>();
        this.buildingRecords = new HashMap<>();
    }


    public int getNewRabbitId() {
        return rabbitIdTagger.getAndIncrement();
    }

    public int getNewCageId() {
        return cageIdTagger.getAndIncrement();
    }

    public int getNewBuildingId() {
        return buildingIdTagger.getAndIncrement();
    }


    //---------------------------------------------- RABBIT METHODS ---------------------------------------------

    public Optional<DbRabbit> findRabbit(int id) {
        DbRabbit rabbit = rabbitRecords.get(id);
        return Optional.ofNullable(rabbit);
    }


    public void addRabbit(DbRabbit rabbit) throws IllegalArgumentException {
        if (rabbitRecords.containsKey(rabbit.getId()))
            throw new IllegalArgumentException("Primary key already in the database.");

        DbRabbit parent = rabbit.getParent();
        if (parent != null)
            parent.addChild(rabbit);

        DbCage cage = rabbit.getCage();
        if (cage != null)
            cage.addRabbit(rabbit);

        rabbitRecords.put(rabbit.getId(), rabbit);
    }


    public void deleteRabbit(int id) throws IllegalArgumentException {
        if (!rabbitRecords.containsKey(id))
            throw new IllegalArgumentException("Entity with primary key " + id + " does not exist");

        DbRabbit rabbit = rabbitRecords.get(id);

        DbRabbit parent = rabbit.getParent();
        if (parent != null)
            parent.getChildren().remove(rabbit);

        DbCage cage = rabbit.getCage();
        if (cage != null)
            cage.getRabbits().remove(rabbit);

        List<DbRabbit> childrenList = rabbit.getChildren();
        for (DbRabbit child : childrenList)
            child.setParent(null);

        rabbitRecords.remove(id);
    }


    //---------------------------------------------- CAGE METHODS ---------------------------------------------

    public Optional<DbCage> findCage(int id) {
        DbCage cage = cageRecords.get(id);
        return Optional.ofNullable(cage);
    }


    public void addCage(DbCage cage) throws IllegalArgumentException {
        if (cageRecords.containsKey(cage.getId()))
            throw new IllegalArgumentException("Primary key already in the database.");

        DbBuilding building = cage.getBuilding();
        if (building != null)
            building.addCage(cage);

        cageRecords.put(cage.getId(), cage);
    }


    public void deleteCage(int id) throws IllegalArgumentException {
        if (!cageRecords.containsKey(id))
            throw new IllegalArgumentException("Entity with primary key " + id + " does not exist");

        // entity to delete
        DbCage cage = cageRecords.get(id);

        DbBuilding building = cage.getBuilding();
        if (building != null)
            building.getCages().remove(cage);

        List<DbRabbit> rabbitList = cage.getRabbits();
        for (DbRabbit rabbit : rabbitList)
            rabbit.nullCage();

        cageRecords.remove(id);
    }


    //---------------------------------------------- BUILDING METHODS ---------------------------------------------

    public Optional<DbBuilding> findBuilding(int id) {
        DbBuilding building = buildingRecords.get(id);
        return Optional.ofNullable(building);
    }


    public void addBuilding(DbBuilding building) throws IllegalArgumentException {
        if (buildingRecords.containsKey(building.getId()))
            throw new IllegalArgumentException("Primary key already in the database.");

        buildingRecords.put(building.getId(), building);
    }


    public void deleteBuilding(int id) throws IllegalArgumentException {
        if (!buildingRecords.containsKey(id))
            throw new IllegalArgumentException("Entity with primary key " + id + " does not exist");

        // entity to delete
        DbBuilding building = buildingRecords.get(id);

        List<DbCage> cageList = building.getCages();
        for (DbCage cage : cageList)
            cage.setBuilding(null);

        buildingRecords.remove(id);
    }
}
