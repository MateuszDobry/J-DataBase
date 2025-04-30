package org.example.rabbits;

import jakarta.persistence.EntityManager;
import org.example.dataBase.entities.DbRabbit;
import org.example.thread.SortingStations;
import org.example.thread.tuples.StartEndOffsetTuple;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Rabbit implements Comparable<Rabbit> {
    private String name;
    private final int age;
    private final float weight;
    private final Set<Rabbit> children;

    // Basically a fancy reference to a constructor method to be used to create set based on parent's type
    private final Supplier<Set<Rabbit>> setFactory;

    // Found that as a self-incrementing value type. Probably the preferred way of doing that, plus it's thread-safe
    private final int id;
    private static final AtomicInteger nextId = new AtomicInteger(1);


    public Rabbit(String name, int age, float weight, Supplier<Set<Rabbit>> setFactory) {
        if (setFactory == null)
            throw new NullPointerException("setFactory is null");

        this.name = name;
        this.age = age;
        this.weight = weight;

        this.setFactory = setFactory;
        this.children = setFactory.get();
        this.id = nextId.getAndIncrement();
    }


    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public float getWeight() {
        return weight;
    }

    public Set<Rabbit> getChildren() {
        return children;
    }

    public int getChildrenNumber() {
        // children of my children are not my children
        return children.size();
    }

    public int getPopulationNumber() {
        return children.size() + children.stream().mapToInt(Rabbit::getPopulationNumber).sum();
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }
    public static void resetId() {
        nextId.set(1);
    }
    public void addChildren() {
        Scanner in = new Scanner(System.in);

        while(true) {
            System.out.println("name, age, weight");

            String input = in.nextLine();
            if (input.isEmpty())
                break;

            try {
                String[] parts = input.split(" ");

                String childName = parts[0];
                int childAge = Integer.parseInt(parts[1]);
                float childWeight = Float.parseFloat(parts[2]);

                Rabbit child = new Rabbit(childName, childAge, childWeight, setFactory);
                this.children.add(child);
            } catch (NumberFormatException e) {
                System.out.println("Wrong arguments");
            } catch (Exception e) {
                return;
            }
        }
    }


    public boolean addChildren(Rabbit child) {
        return this.children.add(child);
    }


    public void printFamily() {
        printFamilyDfs("");
    }


    private  void printFamilyDfs(String depthIndicator) {
        System.out.println(depthIndicator + this);
        depthIndicator += "---";
        for (Rabbit child : children)
            child.printFamilyDfs(depthIndicator);
    }


    //return object if successful
    //return null if we could not find rabbit
    public Rabbit findRabbit(int id) {
        return this.findRabbitDfs(id);
    }


    private Rabbit findRabbitDfs(int id) {
        if (this.id == id)
            return this;

        for (Rabbit child : children) {
            if (child.findRabbitDfs(id) != null)
                return child.findRabbitDfs(id);
        }

        return null;
    }


    public DbRabbit intoDbRabbit(DbRabbit parent) {
        DbRabbit parentRet = new DbRabbit(this.name,this.age,this.weight,parent,null);

        List<DbRabbit> kids = new ArrayList<>();
        for(Rabbit child:children) {
            kids.add(child.intoDbRabbit(parentRet));
        }

        parentRet.addChildren(kids);

        return parentRet;
    }


    public void addSelfAndChildrenToDataBase(EntityManager entityManager, Rabbit parent) {
        //entityManager.persist(this.intoDbRabbit());
        //for(Rabbit children :this.children){

       //}
    }

    // Basically a fancy reference to a function to be performed on the object
    public void populationLvlTraversal(Consumer<Rabbit> action) {
        // Level order traversal - BFS
        Queue<Rabbit> populationQ = new LinkedList<>();

        populationQ.add(this);

        while (!populationQ.isEmpty()) {
            Rabbit currentChild = populationQ.remove();
            action.accept(currentChild);
            populationQ.addAll(currentChild.getChildren());
        }
    }

    public void populationDptTraversal(Consumer<Rabbit> action) {
        // recursive DFS for depth first traversal
        action.accept(this);
        for (Rabbit child : children)
            child.populationDptTraversal(action);
    }


    public void handleSorting(StartEndOffsetTuple startEndOffsets, SortingStations stations) {
        if (children.isEmpty() || startEndOffsets.difference() <= 2) {
            // if you have no children, sort your arraySlice using thread that implements ✨insert sort✨
            // or if you have 2 elements or fewer
            stations.addToSortingQueue(new StartEndOffsetTuple(startEndOffsets));
        } else {
            // spread array between your offspring
            int offspringCount = this.children.size();
            int portion = (startEndOffsets.difference() + 1) / (offspringCount + 1);
            int globalEndOffset = startEndOffsets.endOffset;

            Iterator<Rabbit> childIterator = children.iterator();
            StartEndOffsetTuple offsetTuple = new StartEndOffsetTuple(startEndOffsets.startOffset,startEndOffsets.startOffset + portion);

            if (portion <= 2) {
                Rabbit child = childIterator.next();
                child.handleSorting(startEndOffsets, stations);
                return;
            }

            while (childIterator.hasNext()) {
                Rabbit child = childIterator.next();
                if (!childIterator.hasNext()) { // that's the last one
                    // last child have to get more elements
                    StartEndOffsetTuple tuple = new StartEndOffsetTuple(offsetTuple.startOffset, globalEndOffset);
                    child.handleSorting(tuple, stations);
                    break;
                }
                child.handleSorting(offsetTuple,stations);
                offsetTuple.addToBoth(portion + 1);
            }
        }
    }


    @Override
    public String toString() {
        return "DbRabbit, name: %s, age: %d, weight: %f, children: %d"
                .formatted(name, age, weight, this.getChildrenNumber());
    }


    @Override
    public boolean equals(Object obj) {
        // compare reference, className than stats
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Rabbit other = (Rabbit) obj;

        return name.equals(other.name) && age == other.age &&
                weight == other.weight && children.equals(other.children) &&
                id == other.id;
    }


    @Override
    public int hashCode() {
        // IDK whether we need to hash everything?
        return Objects.hash(name, age, weight, children, id);
    }


    @Override
    public int compareTo(Rabbit other) {
        // first age, that weight, than children
        int result;

        result = Integer.compare(age - other.age, 0);
        if (result != 0)
            return result;

        result = Float.compare(weight - other.weight, 0.0f);
        if (result != 0)
            return result;

        return Integer.compare(this.getChildrenNumber() - other.getChildrenNumber(), 0);
    }


}