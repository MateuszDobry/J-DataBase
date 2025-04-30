package org.example.dataBase.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.dataBase.ZootopiaDbManager;
import org.example.unitTest.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


@Entity
@Table(name = "BUILDINGS")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "cages")
public class DbBuilding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean openForZoo;
    private String address;
    private String phoneNumber;

    // some method to delete these whenever actual record from db is removed
    @OneToMany(mappedBy = "building")
    private List<DbCage> cages;


    public DbBuilding(boolean openForZoo, String address, String phoneNumber, List<DbCage> cages) {
        this.openForZoo = openForZoo;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cages = (cages == null) ? new ArrayList<>() : cages;
    }

    public DbBuilding(String line, ZootopiaDbManager manager) {
        // format: *openFooZoo* *address* *phoneNumber*

        String[] parts = line.split(" ");

        this.openForZoo = Boolean.parseBoolean(parts[0]);
        this.address = parts[1];
        this.phoneNumber = parts[2];

        // we need this cuz later addition will fail if the cages are set to null
        this.cages = new ArrayList<>();
    }

    //------------------------------------------------- REPOSITORY TEST -------------------------------------------------
    public DbBuilding(String line, Repository repository) {
        // format: *openFooZoo* *address* *phoneNumber*

        String[] parts = line.split(" ");

        this.id = repository.getNewBuildingId();

        // we need this cuz later addition will fail if the cages are set to null
        this.cages = new ArrayList<>();

        this.openForZoo = parts.length > 0 ? Boolean.parseBoolean(parts[0]) : false;
        this.address = parts.length > 1 ? parts[1] : "unknown-address";
        this.phoneNumber = parts.length > 2 ? parts[2] : "unknown-phone-number";
    }


    public synchronized void addCage(DbCage cage) {
        this.cages.add(cage);
    }

    public void removeCage(DbCage cage) {
        this.cages.remove(cage);
    }

    public static ArrayList<DbBuilding> getFromFile(String filePath, ZootopiaDbManager manager) {
        ArrayList<DbBuilding> buildings = new ArrayList<>();

        try {
            File buildingFile = new File(filePath);
            Scanner buildingReader = new Scanner(buildingFile);

            while (buildingReader.hasNextLine()) {
                String line = buildingReader.nextLine();

                buildings.add(new DbBuilding(line, manager));
            }

            buildingReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error loading buildings from a file");
            throw new RuntimeException(e);
        }

        return buildings;
    }

    @Override
    public String toString() {
        return String.format("DbBuilding: id: %d, address: %s, number: %s, open for zoo: %b, number of cages: %d",
                    this.id, this.address, this.phoneNumber, this.openForZoo, this.cages.size());
    }
}
