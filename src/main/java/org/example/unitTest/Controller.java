package org.example.unitTest;


import org.example.dataBase.entities.*;

public class Controller
{
    private final Repository repository;


    public Controller(Repository repository) {
        this.repository = repository;
    }


    public String getRabbit(int id) {
        return repository.findRabbit(id)
                .map(DbRabbit::toString)
                .orElse("not found");
    }


    public String putRabbit(String line) {
        try {
            DbRabbit newRabbit = new DbRabbit(line, repository);
            repository.addRabbit(newRabbit);
            return "done";
        } catch (IllegalArgumentException e) {
            return "bad request";
        }
    }


    public String removeRabbit(int id) {
        try {
            repository.deleteRabbit(id);
            return "done";
        } catch (IllegalArgumentException e) {
            return "not found";
        }
    }


     public String getCage(int id) {
        return repository.findCage(id)
                .map(DbCage::toString)
                .orElse("not found");
     }


    public String putCage(String line) {
        try {
            DbCage newCage = new DbCage(line, repository);
            repository.addCage(newCage);
            return "done";
        } catch (IllegalArgumentException e) {
            return "bad request";
        }
    }


    public String removeCage(int id) {
        try {
            repository.deleteCage(id);
            return "done";
        } catch (IllegalArgumentException e) {
            return "not found";
        }
    }


     public String getBuilding(int id) {
        return repository.findBuilding(id)
                .map(DbBuilding::toString)
                .orElse("not found");
     }


    public String putBuilding(String line) {
        try {
            DbBuilding newBuilding = new DbBuilding(line, repository);
            repository.addBuilding(newBuilding);
            return "done";
        } catch (IllegalArgumentException e) {
            return "bad request";
        }
    }


    public String removeBuilding(int id) {
        try {
            repository.deleteBuilding(id);
            return "done";
        } catch (IllegalArgumentException e) {
            return "not found";
        }
    }
}
