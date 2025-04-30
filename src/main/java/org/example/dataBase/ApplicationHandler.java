package org.example.dataBase;

import org.example.dataBase.entities.DbBuilding;
import org.example.dataBase.entities.DbCage;
import org.example.dataBase.entities.DbRabbit;
import org.example.rabbits.Zootopia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class ApplicationHandler {

    private final ZootopiaDbManager manager;
    private boolean applicationRunning;

    public ApplicationHandler(ZootopiaDbManager entityManager){
        this.manager = entityManager;
        this.applicationRunning = false;
    }


    public void run() throws IOException {

        System.out.println("Hello, welcome in our database application! What do you want do to?");
        this.applicationRunning = true;

        Scanner scanner = new Scanner(System.in);

        while (applicationRunning) {
            System.out.println(""" 
                 
                 1 - init exemplary data
                 2 - add data form file
                 3 - add one record
                 4 - delete record
                 5 - show database/part of database
                 6 - show exemplary queries
                 7 - quit \n""");

            String input = scanner.nextLine();

            try {
                int number = Integer.parseInt(input);

                switch (number) {
                    case 0 :
                        manager.loadExemplaryData2(); // hidden option for developers
                        break;
                    case 1 :
                        handleInitExemplaryData("input-data/DbRabbits.txt", manager);
                        break;
                    case 2 :
                        handleAddDataFromFile(scanner);
                        break;
                    case 3 :
                        handleAddOneRecord(scanner);
                        break;
                    case 4 :
                        handleDeleteRecord(scanner);
                        break;
                    case 5:
                        handleShowDataBase(scanner);
                        break;
                    case 6:
                        handleShowExemplaryQueries(scanner);
                        break;
                    case 7 : applicationRunning = false;
                        break;
                    default:
                        System.out.println("Wrong input");
                        break;
                }
            } catch (NumberFormatException  e) {
                System.out.println("Wrong input");
            }
        }
    }

    private void moreExemplaryData() {

    }


    private void handleInitExemplaryData(String filePath, ZootopiaDbManager manager) {
        List<DbRabbit> rabbits = new ArrayList<>();
        List<DbCage> cages = new ArrayList<>();
        Random random = new Random();

        // Read rabbits from the file
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                DbRabbit rabbit = new DbRabbit(line, manager); // Temporarily setting cage to null
                rabbits.add(rabbit);
                manager.addEntity(rabbit);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create cages based on the number of rabbits
        int numberOfCages = (rabbits.size() > 0) ? (int) Math.ceil(rabbits.size() / 3.0) : 1; // Default to 1 cage if no rabbits
        for (int i = 0; i < numberOfCages; i++) {
            List<DbRabbit> rabbitsInCage = new ArrayList<>();
            DbCage cage = new DbCage(i + 1, 0, rabbitsInCage, null); // Initialize with 0 rabbits
            cages.add(cage);
            manager.addEntity(cage);
        }

        // Assign rabbits to cages
        for (int i = 0; i < rabbits.size(); i++) {
            DbRabbit rabbit = rabbits.get(i);
            DbCage cage = cages.get(i % cages.size());
            rabbit.setCage(cage);
            cage.getRabbits().add(rabbit);
            cage.setSize(cage.getSize() + 1); // Update the cage size (it's more like how many Rabbits are in the cage)
            // TODO add Rabbits Counter in the cage instead  of using size and use size to calculate the max number of Cages
        }


        DbBuilding building1 = new DbBuilding(true, "Morska 17b", "769-335-989", new ArrayList<>());
        manager.addEntity(building1);

        // Assign building to cages and add cages to the building's list
        for (DbCage cage : cages) {
            cage.setBuilding(building1);
            building1.getCages().add(cage);
        }
    }


    //---------------------------------------------------------------- File Inserts -------------------------------------------------------------------

    public void handleAddDataFromFile(Scanner scanner) {
        System.out.println("Which entity would you like to upload?");
        System.out.println("""
                1 - DbRabbit\s
                2 - DbCage\s
                3 - DbBuilding\s
                """);
        String input = scanner.nextLine();
        try {
            int number = Integer.parseInt(input);

            switch (number) {
                case 1 :
                    loadFromFile("DbRabbit",scanner);
                    break;
                case 2 :
                    loadFromFile("DbCage",scanner);
                    break;
                case 3 :
                    loadFromFile("DbBuilding",scanner);
                    break;
                default:
                    System.out.println("Wrong input");
                    break;
            }
        } catch (NumberFormatException  e) {
            System.out.println("Wrong input");
        }
    }


    public void loadFromFile(String type, Scanner scanner) {
        if (type.equals("DbRabbit"))  {

            System.out.println("Write path to file");
            String path = scanner.nextLine();

            File file = new File(path);
            if (!file.exists()) {
                System.out.println("This file does not exist");
                return;
            }
            List<DbRabbit> rabbits = manager.getDbFluffle(path);
            manager.addRabbits(rabbits);

        } else if (type.equals("DbCage")) {

            System.out.println("Write path to file");
            String path = scanner.nextLine();

            File file = new File(path);
            if (!file.exists()) {
                System.out.println("This file does not exist");
                return;
            }

            List<DbCage> cages = DbCage.getFromFile(path, manager);
            manager.addCages(cages);

        } else {

            System.out.println("Write path to file");
            String path = scanner.nextLine();

            File file = new File(path);
            if (!file.exists()) {
                System.out.println("This file does not exist");
                return;
            }

            List<DbBuilding> buildings = DbBuilding.getFromFile(path, manager);
            manager.addBuildings(buildings);
        }
    }


    //--------------------------------------------------------------- Manual Inserts ------------------------------------------------------------------

    // TODO implement this method
    public void handleAddOneRecord(Scanner scanner) {
        System.out.println("Who do you want to add?");

        System.out.println("""
                1 - DbRabbit\s
                2 - DbCage\s
                3 - DbBuilding\s
                """);
        String input = scanner.nextLine();
        try {
            int number = Integer.parseInt(input);

            switch (number) {
                case 1 :
                    handleAddOneRabbit(scanner);
                    break;
                case 2 :
                    handleAddOneCage(scanner);
                    break;
                case 3 :
                    handleAddOneBuilding(scanner);
                    break;
                default:
                    System.out.println("Wrong input");
                    break;
            }
        } catch (NumberFormatException  e) {
            System.out.println("Wrong input");
        }
    }

    private void handleAddOneRabbit(Scanner scanner) {
        System.out.println("Rabbit format:\n *name* *age* *weight* *parent id* *cage id*");
        String line = scanner.nextLine();
        DbRabbit rabbit = new DbRabbit(line, manager);
        manager.addEntity(rabbit);
    }

    private void handleAddOneCage(Scanner scanner) {
        System.out.println("Cage format:\n*section* *size* *buildingId*");
        String line = scanner.nextLine();
        DbCage cage = new DbCage(line, manager);
        manager.addEntity(cage);
    }

    private void handleAddOneBuilding(Scanner scanner) {
        System.out.println("Building format:\n *openFooZoo* *address* *phoneNumber*");
        String line = scanner.nextLine();
        DbBuilding building = new DbBuilding(line, manager);
        manager.addEntity(building);
    }


    //--------------------------------------------------------------- Manual Deletes ------------------------------------------------------------------

    private void handleDeleteRecord(Scanner scanner) {
        System.out.println("Who do you want to delete?");

        System.out.println("""
                1 - DbRabbit\s
                2 - DbCage\s
                3 - DbBuilding\s
                """);
        String input = scanner.nextLine();
        try {
            int number = Integer.parseInt(input);

            switch (number) {
                case 1 :
                    handleDeleteOneRabbit(scanner);
                    break;
                case 2 :
                    handleDeleteOneCage(scanner);
                    break;
                case 3 :
                    handleDeleteOneBuilding(scanner);
                    break;
                default:
                    System.out.println("Wrong input");
                    break;
            }
        } catch (NumberFormatException  e) {
            System.out.println("Wrong input");
        }
    }

    private void handleDeleteOneRabbit(Scanner scanner) {
        System.out.println("please input rabbit id");
        // Apparently it needs to get string and then parse it to int because if we do just nextInt(), than the scanner buffer is not flushed?? GG Java
        String input = scanner.nextLine();
        int id = Integer.parseInt(input);

        DbRabbit rabbit = manager.findRabbit(id);

        if (rabbit == null) {
            System.out.println("No such DbRabbit entity found");
            return;
        }

        manager.deleteEntity(rabbit);
    }

    private void handleDeleteOneCage(Scanner scanner) {
        System.out.println("please input cage id");
        String input = scanner.nextLine();
        int id = Integer.parseInt(input);

        DbCage cage = manager.findCage(id);

        if (cage == null) {
            System.out.println("No such DbCage entity found");
            return;
        }

        manager.deleteEntity(cage);
    }

    private void handleDeleteOneBuilding(Scanner scanner) {
        System.out.println("please input building id");
        String input = scanner.nextLine();
        int id = Integer.parseInt(input);

        DbBuilding building = manager.findBuilding(id);

        if (building == null) {
            System.out.println("No such DbRabbit entity found");
            return;
        }

        manager.deleteEntity(building);
    }


    //-------------------------------------------------------------- Show all records -----------------------------------------------------------------

    private void handleShowDataBase(Scanner scanner) {
        System.out.println("Which entity?");

        System.out.println("""
                1 - DbRabbit\s
                2 - DbCage\s
                3 - DbBuilding\s
                """);
        String input = scanner.nextLine();
        int number = Integer.parseInt(input);
        System.out.println("How much data? / All - A");
        String size = scanner.nextLine();
        size = size.toUpperCase();

        try {
            int Size = -1;
            if (!size.equals("A")) {
                Size = Integer.parseInt(size);
            }

        switch (number) {
                case 1 :
                    manager.printFluffle(Size);
                    break;
                case 2 :
                    manager.printCages(Size);
                    break;
                case 3 :
                    manager.printBuildings(Size);
                    break;
                default:
                    System.out.println("Wrong input");
                    break;
            }
        } catch (NumberFormatException  e) {
            System.out.println("Wrong input");
        }

    }


    //------------------------------------------------------------- Show query records ----------------------------------------------------------------

    // TODO implement this method
    private void handleShowExemplaryQueries(Scanner scanner) {
        System.out.println("""
                Chose your Query:
                1 - Find Rabbit's older/younger/equal [ ] age
                2 - Show all Rabbit's from building nr [ ]
                3 - Show top 3 fattest rabbits
                4 - Show buildings which are opened to pettingZoo
                5 - Cages with most Rabbits
                """);
        String input = scanner.nextLine();
        try {
            int number = Integer.parseInt(input);

            switch (number) {
                case 1:
                    String age = "";
                    int ageInt = 0;
                    System.out.println("""
                            A - Age above
                            B - Age below
                            E - Equal age 
                            """);
                    String choice = scanner.nextLine();
                    choice = choice.toLowerCase();
                    switch (choice) {
                        case "a":
                            System.out.println("Write age you want your Rabbits to be above:");
                            age = scanner.nextLine();
                            ageInt = Integer.parseInt(age);
                            manager.findByAge(ageInt, 0);
                            break;
                        case "b":
                            System.out.println("Write age you want your Rabbits to be below:");
                            age = scanner.nextLine();
                            ageInt = Integer.parseInt(age);
                            manager.findByAge(ageInt, 1);
                            break;
                        case "e":
                            System.out.println("Write age you want your Rabbits to be equal:");
                            age = scanner.nextLine();
                            ageInt = Integer.parseInt(age);
                            manager.findByAge(ageInt, 2);
                            break;
                        default:
                            System.out.println("Wrong input");
                            break;
                    }
                    break;
                case 2:
                    System.out.println("Which building?");
                    int buildingNumber = Integer.parseInt(scanner.nextLine());
                    manager.printRabbitsFromBuildings(buildingNumber);
                    break;
                case 3:
                    manager.printTop3FattestRabbits();
                    break;
                case 4:
                    manager.buildingsOpenedForZoo();
                    break;
                case 5:
                    int counter = 0;
                    System.out.println("How many cages?");
                    counter = Integer.parseInt(scanner.nextLine());
                    manager.cagesWithMostRabbits(counter);
                    break;
                default:
                    System.out.println("Wrong input");
                    break;
            }

        } catch (NumberFormatException  e) {
            System.out.println("Wrong input");
        }

    }
}
