package org.example;

import org.example.dataBase.entities.DbBuilding;
import org.example.dataBase.entities.DbCage;
import org.example.dataBase.entities.DbRabbit;
import org.example.unitTest.Repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Optional;


public class DatabaseRepositoryTest
{
    private Repository repo;


    @BeforeEach
    public void setup() {
        this.repo = new Repository();
    }


    @AfterEach
    public void shutdown() {
        this.repo = null;
    }

    //---------------------------------------- Rabbit Tests ------------------------------------------------------------
    // Addition Tests
    @Test
    public void whenGivenNewRabbit_addRabbitInsertsNewRabbit() {
        // remember about  ** new ArrayList<DbRabbit>() ** in the constructor -> we compensate for it sometimes, and sometimes we don't, and it will nuke the test
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Syn Pawła", 1, 2.5f, null, rabbit1, new ArrayList<DbRabbit>());


        // add 1 rabbit test
        assertThatCode(() -> repo.addRabbit(rabbit1)).doesNotThrowAnyException();
        // add rabbit with new id test
        assertThatCode(() -> repo.addRabbit(rabbit2)).doesNotThrowAnyException();
    }


    @Test
    public void whenGivenInsertedRabbit_addRabbitThrowsIllegalArgumentException() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());

        assertThatCode(() -> repo.addRabbit(rabbit1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.addRabbit(rabbit1)).isInstanceOf(IllegalArgumentException.class);
    }


    // Search Tests
    @Test
    public void whenGivenExistingRabbit_findRabbitReturnsRabbit() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit1);
        Optional<DbRabbit> foundRabbit;

        foundRabbit = repo.findRabbit(1);

        assertThat(foundRabbit).isPresent();
        assertThat(foundRabbit.get()).isEqualTo(rabbit1);
    }


    @Test
    public void whenGivenNonExistingRabbit_findRabbitReturnsEmptyOptional() {
        Optional<DbRabbit> foundRabbit;

        foundRabbit = repo.findRabbit(420);

        assertThat(foundRabbit).isEmpty();
    }


    // Removal Tests
    @Test
    public void whenGivenInsertedRabbit_deleteRabbitDeletesRabbit() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit1);
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Syn Pawła", 1, 2.5f, null, rabbit1, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit2);

        // delete 1 rabbit test
        assertThatCode(() -> repo.deleteRabbit(1)).doesNotThrowAnyException();
        // delete another rabbit test
        assertThatCode(() -> repo.deleteRabbit(2)).doesNotThrowAnyException();
    }


    @Test
    public void whenGivenDeletedRabbit_deleteRabbitThrowsIllegalArgumentException() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit1);

        // delete the same rabbit again test
        assertThatCode(() -> repo.deleteRabbit(1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.deleteRabbit(1)).isInstanceOf(IllegalArgumentException.class);
    }


    // Children Lists Tests
    @Test
    public void whenAddingNewChildRabbit_addRabbitUpdatesParentChildrenArrayList() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Syn Pawła", 1, 2.5f, null, rabbit1, new ArrayList<DbRabbit>());

        // rabbit children ArrayList addition test
        repo.addRabbit(rabbit1);
        repo.addRabbit(rabbit2);

        DbRabbit foundRabbit = repo.findRabbit(1).get();
        assertThat(foundRabbit.getChildren().get(0)).isEqualTo(rabbit2);
    }


    @Test
    public void whenDeletingChildRabbit_deleteRabbitUpdatesParentChildrenArrayList() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Syn Pawła", 1, 2.5f, null, rabbit1, new ArrayList<DbRabbit>());

        // rabbit children ArrayList deletion test
        repo.addRabbit(rabbit1);
        repo.addRabbit(rabbit2);
        repo.deleteRabbit(2);

        DbRabbit foundRabbit = repo.findRabbit(1).get();
        assertThat(foundRabbit.getChildren()).isEmpty();
    }


    @Test
    public void whenDeletingParentRabbit_deleteRabbitUpdatesChildrenParentReference() {
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Syn Pawła", 1, 2.5f, null, rabbit1, new ArrayList<DbRabbit>());

        // rabbit children ArrayList deletion test
        repo.addRabbit(rabbit1);
        repo.addRabbit(rabbit2);
        repo.deleteRabbit(1);

        DbRabbit foundRabbit = repo.findRabbit(2).get();
        assertThat(foundRabbit.getParent()).isNull();
    }


    @Test
    public void whenDeletingRabbit_deleteRabbitUpdatesCageRabbitsArrayList() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137,new ArrayList<>(),null);
        repo.addCage(cage1);
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 2, 2.5f, cage1, null, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit1);

        // rabbit children ArrayList deletion test
        repo.deleteRabbit(1);

        DbCage foundCage = repo.findCage(1).get();
        assertThat(foundCage.getRabbits()).isEmpty();
    }


    //---------------------------------------- Cage Tests ------------------------------------------------------------

    // Addition Tests
    @Test
    public void whenGivenNewCage_addCageInsertsNewCage() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137,new ArrayList<>(),null);
        DbCage cage2 = new DbCage(repo.getNewCageId(), 420,2137,new ArrayList<>(),null);

        assertThatCode(() -> repo.addCage(cage1)).doesNotThrowAnyException();
        assertThatCode(() -> repo.addCage(cage2)).doesNotThrowAnyException();
    }


    @Test
    public void whenGivenInsertedCage_addCageThrowsIllegalArgumentException() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137,new ArrayList<>(),null);

        assertThatCode(() -> repo.addCage(cage1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.addCage(cage1)).isInstanceOf(IllegalArgumentException.class);
    }


    // Search Tests
    @Test
    public void whenGivenExistingCage_findCageReturnsRabbit() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137,new ArrayList<>(),null);
        repo.addCage(cage1);
        Optional<DbCage> foundCage;

        foundCage = repo.findCage(1);

        assertThat(foundCage).isPresent();
        assertThat(foundCage.get()).isEqualTo(cage1);
    }


    @Test
    public void whenGivenNonExistingCage_findCageReturnsEmptyOptional() {
        Optional<DbCage> foundCage;

        foundCage = repo.findCage(420);

        assertThat(foundCage).isEmpty();
    }


    // Removal Tests
    @Test
    public void whenGivenInsertedCage_deleteCageDeletesCage() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(),null);
        repo.addCage(cage1);
        DbCage cage2 = new DbCage(repo.getNewCageId(),421,2138, new ArrayList<>(),null);
        repo.addCage(cage2);

        // delete 1 rabbit test
        assertThatCode(() -> repo.deleteCage(1)).doesNotThrowAnyException();
        // delete another rabbit test
        assertThatCode(() -> repo.deleteCage(2)).doesNotThrowAnyException();
    }


    @Test
    public void whenGivenDeletedCage_deleteCageThrowsIllegalArgumentException() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(),null);
        repo.addCage(cage1);

        // delete the same cage again test
        assertThatCode(() -> repo.deleteCage(1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.deleteCage(1)).isInstanceOf(IllegalArgumentException.class);
    }


    // Children Lists Tests
    @Test
    public void whenAddingNewCage_addCageUpdatesBuildingCagesArrayList() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(), true, "...", "...", new ArrayList<>());
        repo.addBuilding(building1);
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(), building1);
        repo.addCage(cage1);

        // Building's cages deletion test
        DbBuilding foundBuilding = repo.findBuilding(1).get();

        assertThat(foundBuilding.getCages().get(0)).isEqualTo(cage1);
    }


    @Test
    public void whenDeletingCage_addCageUpdatesBuildingCagesArrayList() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(), true, "...", "...", new ArrayList<>());
        repo.addBuilding(building1);
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(), building1);
        repo.addCage(cage1);

        // Building's cages deletion test
        repo.deleteCage(1);

        DbBuilding foundBuilding = repo.findBuilding(1).get();

        assertThat(foundBuilding.getCages()).isEmpty();
    }


    @Test
    public void whenDeletingCage_deletedCageUpdatesCagesRabbitsReference() {
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137,new ArrayList<>(),null);
        repo.addCage(cage1);
        DbRabbit rabbit1 = new DbRabbit(repo.getNewRabbitId(), "Paweł", 22, 2.5f, cage1, null, new ArrayList<DbRabbit>());
        DbRabbit rabbit2 = new DbRabbit(repo.getNewRabbitId(), "Paweł2", 2, 2.5f, cage1, null, new ArrayList<DbRabbit>());
        repo.addRabbit(rabbit1);
        repo.addRabbit(rabbit2);

        // cage deletion test
        repo.deleteCage(1);

        assertThat(rabbit1.getCage()).isNull();
        assertThat(rabbit2.getCage()).isNull();
    }


    //---------------------------------------- Building Tests ------------------------------------------------------------


    // TODO buildings tests

    @Test
    public void whenGivenNewBuilding_addBuildingInsertsNewBuilding() {
        DbBuilding building1 = new DbBuilding(false,"meow meow meow street","213-751-123",null);
        building1.setId(repo.getNewBuildingId());

        DbBuilding building2 = new DbBuilding(false,"rawr rawr street","123-213-769",null);
        building2.setId(repo.getNewBuildingId());

        assertThatCode(() -> repo.addBuilding(building1)).doesNotThrowAnyException();
        assertThatCode(() -> repo.addBuilding(building2)).doesNotThrowAnyException();
    }

    @Test
    public void whenGivenInsertedBuilding_addBuildingThrowsIllegalArgumentException() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(),true,"Partyzantow","+48 738 764 938",new ArrayList<>());

        assertThatCode(() -> repo.addBuilding(building1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.addBuilding(building1)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    public void whenGivenExistingBuilding_findBuildingReturnsBuilding() {
        DbBuilding building1 = new DbBuilding(false,"meow meow meow street","213-751-123",new ArrayList<>());
        building1.setId(2137);
        repo.addBuilding(building1);

        Optional<DbBuilding> foundBuilding;

        foundBuilding = repo.findBuilding(2137);

        assertThat(foundBuilding).isPresent();
        assertThat(foundBuilding.get()).isEqualTo(building1);
    }

    @Test
    public void whenGivenNonExistingBuilding_findBuildingReturnsEmptyOptional() {
        Optional<DbBuilding> foundBuilding;

        foundBuilding = repo.findBuilding(420);

        assertThat(foundBuilding).isEmpty();
    }

    @Test
    public void whenGivenInsertedBuilding_deleteBuildingDeletesBuilding() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(),true,"Partyzantow","+48 738 764 938",new ArrayList<>());
        repo.addBuilding(building1);
        DbBuilding building2 = new DbBuilding(repo.getNewBuildingId(),false,"Małowiecka","+48 777 983 846",new ArrayList<>());
        repo.addBuilding(building2);

        // delete 1 building test
        assertThatCode(() -> repo.deleteBuilding(1)).doesNotThrowAnyException();
        // delete another building test
        assertThatCode(() -> repo.deleteBuilding(2)).doesNotThrowAnyException();
    }

    @Test
    public void whenGivenDeletedBuilding_deleteBuildingThrowsIllegalArgumentException() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(),false,"Nigska","+48 736 536 882",new ArrayList<>());
        repo.addBuilding(building1);

        // delete the same building again test
        assertThatCode(() -> repo.deleteBuilding(building1.getId())).doesNotThrowAnyException();
        assertThatThrownBy(() -> repo.deleteBuilding(building1.getId())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void whenDeletingBuilding_deletedBuildingUpdatesBuildingsCagesReference() {
        DbBuilding building1 = new DbBuilding(repo.getNewBuildingId(), false,"Barczewska","+48 777 333 465",new ArrayList<>());
        repo.addBuilding(building1);
        DbCage cage1 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(), building1);
        DbCage cage2 = new DbCage(repo.getNewCageId(),420,2137, new ArrayList<>(), building1);
        repo.addCage(cage1);
        repo.addCage(cage2);

        // building deletion test
        repo.deleteBuilding(1);

        assertThat(cage1.getBuilding()).isNull();
        assertThat(cage2.getBuilding()).isNull();
    }
}
