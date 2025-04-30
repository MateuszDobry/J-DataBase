package org.example.dataBase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.RunConfig;
import org.example.dataBase.entities.*;
import org.example.rabbits.Zootopia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ZootopiaDbManager {

    private final EntityManagerFactory emf;
    private final EntityManager entityManager;
    private List<DbRabbit> dbFluffle;
    private final RunConfig config;


    public static void main(String[] args) {
        ZootopiaDbManager manager = new ZootopiaDbManager();
        ApplicationHandler application = new ApplicationHandler(manager);

        try {
            application.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        manager.close();
    }


    public ZootopiaDbManager(Zootopia zootopia, RunConfig config) {
        this.emf = Persistence.createEntityManagerFactory("Rabbit Database");
        this.entityManager = emf.createEntityManager();
        this.dbFluffle = zootopia.getDbFluffle();
        this.config = config;
    }

    public ZootopiaDbManager() {
        this.emf = Persistence.createEntityManagerFactory("Rabbit Database");
        this.entityManager = emf.createEntityManager();
        this.dbFluffle = null;
        String[] config = new String[0];
        this.config = new RunConfig(config);
    }

    public void close() {
        this.emf.close();
        this.entityManager.close();
    }


    //------------------------------ File's of Entities Utils -------------------------------

    public void loadDbFluffle(String fileName) {
        Zootopia zootopia = new Zootopia(config);
        zootopia.initFluffleFromFile(fileName);
        this.dbFluffle = zootopia.getDbFluffle();
        loadFluffleToDataBase();
    }


    // path to file where rabbits are stored
    public List<DbRabbit> getDbFluffle(String path) {
        Zootopia zootopia = new Zootopia();
        zootopia.initFluffleFromFile(path);
        return zootopia.getDbFluffle();
    }


    // is this needed? This doesn't integrate with cage's and parent's ArrayLists
    public void loadFluffleToDataBase() {
        entityManager.getTransaction().begin();

        for(DbRabbit family : dbFluffle) {
            entityManager.persist(family);
        }

        entityManager.getTransaction().commit(); //input-data/in1.txt
    }


    //------------------------------------------------------------------ Queries ----------------------------------------------------------------------

    public void printFluffle(int size) {
        List<DbRabbit> rabbits = entityManager.createQuery("SELECT r FROM DbRabbit r", DbRabbit.class).getResultList();
        if(size == -1)
            size = rabbits.size();

        for(int i = 0 ; i < size && i < rabbits.size(); i++)
            System.out.println(rabbits.get(i).toString());
    }


    public void printCages(int size) {
        List<DbCage> cages = entityManager.createQuery("SELECT cage FROM DbCage cage", DbCage.class).getResultList();
        if(size == -1)
            size = cages.size();

        for(int i = 0 ; i < size && i < cages.size(); i++)
            System.out.println(cages.get(i).toString());
    }


    public void printBuildings(int size) {
        List<DbBuilding> buildings = entityManager.createQuery("SELECT building FROM DbBuilding building", DbBuilding.class).getResultList();
        if(size == -1)
            size = buildings.size();

        for(int i = 0 ; i < size && i < buildings.size(); i++)
            System.out.println(buildings.get(i).toString());
    }


    public DbRabbit findRabbit(int rabbitID) {
        return entityManager.find(DbRabbit.class, rabbitID);
    }

    public DbCage findCage(int cageID) {
        return entityManager.find(DbCage.class, cageID);
    }

    public DbBuilding findBuilding(int buildingID) {
        return entityManager.find(DbBuilding.class, buildingID);
    }


    public void findByAge(int age, int isOlder) {
        String queryString = "";

        if (isOlder == 0) {
            queryString = "SELECT rabbit FROM DbRabbit rabbit WHERE rabbit.age > :age"; // Older
        } else if (isOlder == 1) {
            queryString = "SELECT rabbit FROM DbRabbit rabbit WHERE rabbit.age < :age"; // Younger
        } else if (isOlder == 2) {
            queryString = "SELECT rabbit FROM DbRabbit rabbit WHERE rabbit.age = :age"; // Equal
        }

        List<DbRabbit> rabbits = entityManager.createQuery(queryString, DbRabbit.class)
                .setParameter("age", age)
                .getResultList();

        rabbits.forEach(System.out::println);
    }

    public void printRabbitsFromBuildings(int buildingId) {
        List<DbRabbit> rabbits = entityManager.createQuery(
                        "SELECT r FROM DbRabbit r JOIN r.cage c WHERE c.building.id = :buildingId", DbRabbit.class)
                .setParameter("buildingId", buildingId)
                .getResultList();

        if (rabbits.isEmpty()) {
            System.out.println("No rabbits found in building with ID: " + buildingId);
        } else {
            rabbits.forEach(rabbit -> System.out.println(rabbit.toString()));
        }
    }

    public void printTop3FattestRabbits() {
        List<DbRabbit> rabbits = entityManager.createQuery(
                        "SELECT r FROM DbRabbit r ORDER BY r.weight DESC", DbRabbit.class)
                .setMaxResults(3)
                .getResultList();

        rabbits.forEach(rabbit -> System.out.println(rabbit.toString()));
    }

    public void buildingsOpenedForZoo() {
        List<DbBuilding> openBuildings = entityManager.createQuery(
                        "SELECT b FROM DbBuilding b WHERE b.openForZoo = true", DbBuilding.class)
                .getResultList();

        openBuildings.forEach(building -> System.out.println(building.toString()));
    }

    public void cagesWithMostRabbits(int topN) {
        List<DbCage> cages = entityManager.createQuery(
                        "SELECT c FROM DbCage c LEFT JOIN c.rabbits r GROUP BY c ORDER BY COUNT(r) DESC", DbCage.class)
                .setMaxResults(topN)
                .getResultList();

        cages.sort(new DbCageRabbitCountComparator());
        cages.forEach(cage -> {
            long rabbitCount = cage.getRabbits().size();
            System.out.println("Cage ID: " + cage.getId() + ", Number of Rabbits: " + rabbitCount);
        });
    }










    //------------------------------------------------------------------ Inserts ---------------------------------------------------------------------

    //---------------------------------- Singular Entities ----------------------------------

    public void addEntity(DbBuilding building) {
        entityManager.getTransaction().begin();
        entityManager.persist(building);
        entityManager.getTransaction().commit();
    }


    public void addEntity(DbCage cage) {
        entityManager.getTransaction().begin();
        entityManager.persist(cage);
        entityManager.getTransaction().commit();
        DbBuilding building = cage.getBuilding();

        if (building != null)
            building.addCage(cage);
    }


    public void addEntity(DbRabbit rabbit) {
        entityManager.getTransaction().begin();
        entityManager.persist(rabbit);
        entityManager.getTransaction().commit();
        DbCage cage = rabbit.getCage();

        if (cage != null)
            cage.addRabbit(rabbit);

        DbRabbit parent = rabbit.getParent();

        if (parent != null)
            parent.addChild(rabbit);
    }


    //--------------------------------- List's of Entities ----------------------------------
    // 1 transaction for all entities

    public void addBuildings(List<DbBuilding> buildings) {
        entityManager.getTransaction().begin();

        for (DbBuilding building : buildings)
            entityManager.persist(building);

        entityManager.getTransaction().commit();
    }


    public void addCages(List<DbCage> cages) {
        entityManager.getTransaction().begin();

        for (DbCage cage: cages) {
            entityManager.persist(cage);
            DbBuilding building = cage.getBuilding();

            if (building != null)
                building.addCage(cage);
        }

        entityManager.getTransaction().commit();
    }


    public void addRabbits(List<DbRabbit> rabbits) {
        entityManager.getTransaction().begin();

        for (DbRabbit rabbit: rabbits) {
            entityManager.persist(rabbit);
            DbCage cage = rabbit.getCage();

            if (cage != null)
                cage.addRabbit(rabbit);

            DbRabbit parent = rabbit.getParent();

            if (parent != null)
                parent.addChild(rabbit);
        }

        entityManager.getTransaction().commit();
    }


    public void examploryAddition() {
        DbBuilding b1 = new DbBuilding(true, "ExampleAddress/ExampleStreet/ExampleNumber", "123 456 789", null);
        DbCage c1 = new DbCage(1, 1, null, b1);
        DbCage c2 = new DbCage(1, 1, null, b1);
        DbRabbit r1 = new DbRabbit("Ziomal", 12, 12, null, null, c1);
        addEntity(b1);
        addEntity(c1);
        addEntity(c2);
        addEntity(r1);
    }


    //------------------------------------------------------------------ Deletes ---------------------------------------------------------------------

    public void deleteEntity(DbRabbit rabbit) {
        entityManager.getTransaction().begin();

        DbRabbit parent = rabbit.getParent();
        if (parent != null)
            parent.getChildren().remove(rabbit);

        DbCage cage = rabbit.getCage();
        if (cage != null)
            cage.getRabbits().remove(rabbit);

        entityManager.remove(entityManager.contains(rabbit) ? rabbit : entityManager.merge(rabbit));

        entityManager.getTransaction().commit();
    }


    public void deleteEntity(DbCage cage) {
        entityManager.getTransaction().begin();

        DbBuilding building = cage.getBuilding();
        if (building != null)
            building.getCages().remove(cage);

        List<DbRabbit> rabbits = cage.getRabbits();
        for (DbRabbit rabbit : rabbits)
            rabbit.setCage(null);

        entityManager.remove(entityManager.contains(cage) ? cage : entityManager.merge(cage));

        entityManager.getTransaction().commit();
    }


    public void deleteEntity(DbBuilding building) {
        entityManager.getTransaction().begin();

        List<DbCage> cages = building.getCages();
        for (DbCage cage : cages)
            cage.setBuilding(null);

        entityManager.remove(entityManager.contains(building) ? building : entityManager.merge(building));

        entityManager.getTransaction().commit();
    }

    public void loadExemplaryData2() {
        System.out.println("Loading huge database...");
        ArrayList<DbBuilding> buildings = DbBuilding.getFromFile("input-data/buildings.txt",this);
        addBuildings(buildings);

        ArrayList<DbCage> cages = DbCage.getFromFile("input-data/cages.txt",this);
        addCages(cages);

        List<DbRabbit> rabbits = getDbFluffle("input-data/in4.txt");
        //this.entityManager.getTransaction().begin();
        this.addRabbits(rabbits);
        //this.entityManager.getTransaction().commit();

        List<DbRabbit> rabbitList = this.entityManager.createQuery("SELECT r FROM DbRabbit r", DbRabbit.class).getResultList();

        Random random = new Random();

        for (DbRabbit rabbit : rabbitList) {
            rabbit.setCage(cages.get(Math.abs(random.nextInt(cages.size()))));
        }
    }
}
