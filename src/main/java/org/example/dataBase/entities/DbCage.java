package org.example.dataBase.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.dataBase.ZootopiaDbManager;
import org.example.unitTest.Repository;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


@Entity
@Table(name = "CAGES")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DbCage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int section;
    // in square meters
    private int size;

    // some method to delete these whenever actual record from db is removed
    @OneToMany(mappedBy = "cage")
    private List<DbRabbit> rabbits;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL) // Hibernate specific otherwise we need to do it manually :(
    private DbBuilding building;


    public DbCage(int section, int size, List<DbRabbit> rabbits, DbBuilding building) {
        this.section = section;
        this.size = size;
        this.rabbits = (rabbits == null) ? new ArrayList<>() : rabbits;
        this.building = building;
    }

    public DbCage(String line, ZootopiaDbManager manager) {
        // format: *section* *size* *buildingId*
        String[] parts = line.split(" ");

        this.section = Integer.parseInt(parts[0]);
        this.size = Integer.parseInt(parts[1]);

        // we need this cuz later addition will fail if the cages are set to null
        this.rabbits = new ArrayList<>();

        if (parts.length > 2 ) {
            int buildingId = Integer.parseInt(parts[2]);
            this.building = manager.findBuilding(buildingId);
        }
    }

    //------------------------------------------------- REPOSITORY TEST -------------------------------------------------
    public DbCage(String line, Repository repository) {
        // format: *section* *size* *buildingId*
        String[] parts = line.split(" ");

        this.id = repository.getNewCageId();

        // we need this cuz later addition will fail if the cages are set to null
        this.rabbits = new ArrayList<>();

        this.section = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        this.size = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
        int buildingId = parts.length > 2 ? Integer.parseInt(parts[2]) : -1;

        this.building = buildingId != -1 ? repository.findBuilding(buildingId).orElse(null) : null;
    }


    public void printRabbits() {
        for (DbRabbit rabbit : rabbits) {
            System.out.println(rabbit.toStringWithoutId());
        }
    }

    public synchronized void addRabbit(DbRabbit rabbit) {
        this.rabbits.add(rabbit);
    }


    public static ArrayList<DbCage> getFromFile(String filePath, ZootopiaDbManager manager) {
        ArrayList<DbCage> cages = new ArrayList<>();

        try {
            File cageFile = new File(filePath);
            Scanner cageReader = new Scanner(cageFile);

            while (cageReader.hasNextLine()) {
                String line = cageReader.nextLine();

                cages.add(new DbCage(line, manager));
            }

            cageReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error loading cages from a file");
            throw new RuntimeException(e);
        }

        return cages;
    }

    @Override
    public String toString() {
        return String.format("DbCage: id: %d, section: %s, size: %dm3, number of rabbits: %d, building: %d",
                this.id,  this.section, this.size, this.rabbits.size(), (this.building == null) ? -130 : this.building.getId());
    }
}
