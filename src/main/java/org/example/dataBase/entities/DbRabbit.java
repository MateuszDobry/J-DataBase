package org.example.dataBase.entities;

import org.example.dataBase.ZootopiaDbManager;
import org.example.unitTest.Repository;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "RABBITS")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class DbRabbit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private int age;
    private float weight;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private DbCage cage;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private DbRabbit parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbRabbit> children;


    public DbRabbit(String name, int age, float weight, DbRabbit parent, List<DbRabbit> children, DbCage cage) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.parent = parent;
        this.children = children;
        this.cage = cage;
    }

    public DbRabbit(String name, int age, float weight, DbRabbit parent, List<DbRabbit> children) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.parent = parent;
        this.children = children;
        this.cage = null;
    }

    public DbRabbit(String line, ZootopiaDbManager manager) {
        String[] parts = line.split(" ");

        this.children = new ArrayList<>();
        this.parent = null;
        this.cage = null;

        this.name = parts.length > 0 ? parts[0] : "Unknown";
        this.age = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        this.weight = parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0f;
        int parentId = parts.length > 3 ? Integer.parseInt(parts[3]) : -1;
        int cageId = parts.length > 4 ? Integer.parseInt(parts[4]) : -1;

        if (parentId != -1)
            this.parent = manager.findRabbit(parentId);

        if (cageId != -1)
            this.cage = manager.findCage(cageId);
    }

    //------------------------------------------------- REPOSITORY TEST -------------------------------------------------
    public DbRabbit(String line, Repository repository) {
        String[] parts = line.split(" ");

        this.id = repository.getNewRabbitId();

        this.children = new ArrayList<>();

        this.name = parts.length > 0 ? parts[0] : "Unknown";
        this.age = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        this.weight = parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0f;
        int parentId = parts.length > 3 ? Integer.parseInt(parts[3]) : -1;
        int cageId = parts.length > 4 ? Integer.parseInt(parts[4]) : -1;

        // if we find the parent we set it if not the rabbit is parent-less - no throw
        this.parent = parentId != -1 ? repository.findRabbit(parentId).orElse(null) : null;
        this.cage = cageId != -1 ? repository.findCage(cageId).orElse(null) : null;
    }


    public synchronized void addChild(DbRabbit rabbit) {
        this.children.add(rabbit);
    }

    public void addChildren(List<DbRabbit> children) {
        this.children = children;
    }

    public void setCage(DbCage cage) {
       this.cage = cage;
       cage.addRabbit(this);
    }

    public void nullCage() {
        this.cage = null;
    }


    public String toStringWithoutId(){
        return "name: %s, age: %d, weight: %f, cage: %s ,children_count : %d"
                .formatted(name, age, weight, cage, children.size());
    }

    @Override
    public String toString() {
        return "DbRabbit id: %d, name: %s, age: %d, weight: %.2f, cageId: %d, children_count : %d"
                .formatted(id, name, age, weight, (cage == null ? -130 : cage.getId()), children.size());
    }
}
