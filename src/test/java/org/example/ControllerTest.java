package org.example;

import org.example.dataBase.entities.DbBuilding;
import org.example.dataBase.entities.DbCage;
import org.example.dataBase.entities.DbRabbit;
import org.example.unitTest.Controller;
import org.example.unitTest.Repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class ControllerTest
{
    @Mock
    private Repository mockRepo;

    @InjectMocks
    private Controller ctrl;


    // Rabbit Search Tests
    @Test
    public void whenGivenExistingRabbit_getReturnsRabbitToString() {
        DbRabbit rabbit = new DbRabbit(1, "Paweł", 2, 2.5f, null, null, new ArrayList<DbRabbit>());
        when(mockRepo.findRabbit(1)).thenReturn(Optional.of(rabbit));

        String result = ctrl.getRabbit(1);

        // IMPORTANT INFORMATION FLOAT FORMATING IN JAVA IS NOT REALLY HELPFUL - CHANGE ',' to '.' HOWEVER U LIKE
        String expected = "DbRabbit id: 1, name: Paweł, age: 2, weight: 2,50, cageId: -130, children_count : 0";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenGivenNonExistingRabbit_getReturnsNotFound() {
        when(mockRepo.findRabbit(1)).thenReturn(Optional.empty());

        String result = ctrl.getRabbit(1);

        String expected = "not found";
        assertThat(result).isEqualTo(expected);
    }


    // Rabbit Addition Tests
    @Test
    public void whenAddingRabbitFromString_putConstructsCorrectRabbit() {
        String rabbitLine = "Paweł 2 2.5 -1 -1";
        ArgumentCaptor<DbRabbit> captor = ArgumentCaptor.forClass(DbRabbit.class);

        /* repo setup */
        when(mockRepo.getNewRabbitId()).thenReturn(1);
        doNothing().when(mockRepo).addRabbit(Mockito.any(DbRabbit.class));


        ctrl.putRabbit(rabbitLine);

        verify(mockRepo).addRabbit(captor.capture());
        DbRabbit capturedRabbit = captor.getValue();

        assertThat(capturedRabbit.getId()).isEqualTo(1);
        assertThat(capturedRabbit.getName()).isEqualTo("Paweł");
        assertThat(capturedRabbit.getAge()).isEqualTo(2);
        assertThat(capturedRabbit.getWeight()).isEqualTo(2.5f);
        assertThat(capturedRabbit.getCage()).isEqualTo(null);
        assertThat(capturedRabbit.getChildren().size()).isEqualTo(0);
    }

    @Test
    public void whenAddingNewRabbitFromString_putRabbitReturnsDone() {
        String rabbitLine = "Paweł 2 2.5 -1 -1";

        /* repo setup */
        when(mockRepo.getNewRabbitId()).thenReturn(1);
        doNothing().when(mockRepo).addRabbit(Mockito.any(DbRabbit.class));

        String result = ctrl.putRabbit(rabbitLine);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenAddingExistingRabbit_putRabitReturnsBadRequest() {
        String rabbitLine = "Paweł 2 2.5 -1 -1";

        /* repo setup */
        when(mockRepo.getNewRabbitId()).thenReturn(1);
        doThrow(new IllegalArgumentException()).when(mockRepo).addRabbit(Mockito.any(DbRabbit.class));

        String result = ctrl.putRabbit(rabbitLine);

        assertThat(result).isEqualTo("bad request");
    }


    @Test
    public void whenDeletingExistingRabbit_deleteReturnsDone() {
        doNothing().when(mockRepo).deleteRabbit(1);

        String result = ctrl.removeRabbit(1);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenDeletingNonExistingRabbit_deleteReturnsNotFound() {
        doThrow(new IllegalArgumentException()).when(mockRepo).deleteRabbit(1);

        String result = ctrl.removeRabbit(1);

        assertThat(result).isEqualTo("not found");
    }


    //---------------------------------------- Cage Tests ------------------------------------------------------------

    // Cage Search Tests
    @Test
    public void whenGivenExistingCage_getReturnsCageToString() {
        DbCage cage = new DbCage(1,420,2137,new ArrayList<>(),null);
        when(mockRepo.findCage(1)).thenReturn(Optional.of(cage));

        String result = ctrl.getCage(1);

        // IMPORTANT INFORMATION FLOAT FORMATING IN JAVA IS NOT REALLY HELPFUL - CHANGE ',' to '.' HOWEVER U LIKE
        String expected = "DbCage: id: 1, section: 420, size: 2137m3, number of rabbits: 0, building: -130";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenGivenNonExistingCage_getReturnsNotFound() {
        when(mockRepo.findCage(1)).thenReturn(Optional.empty());

        String result = ctrl.getCage(1);

        String expected = "not found";
        assertThat(result).isEqualTo(expected);
    }


    // Cage Addition Tests
    @Test
    public void whenAddingCageFromString_putConstructsCorrectCage() {
        String cageLine = "420 2137 -1";
        ArgumentCaptor<DbCage> captor = ArgumentCaptor.forClass(DbCage.class);

        /* repo setup */
        when(mockRepo.getNewCageId()).thenReturn(1);
        doNothing().when(mockRepo).addCage(Mockito.any(DbCage.class));


        ctrl.putCage(cageLine);

        verify(mockRepo).addCage(captor.capture());
        DbCage capturedCage = captor.getValue();

        assertThat(capturedCage.getId()).isEqualTo(1);
        assertThat(capturedCage.getSection()).isEqualTo(420);
        assertThat(capturedCage.getSize()).isEqualTo(2137);
    }

    @Test
    public void whenAddingNewCageFromString_putCageReturnsDone() {
        String cageLine = "420 2137 -1";

        /* repo setup */
        when(mockRepo.getNewCageId()).thenReturn(1);
        doNothing().when(mockRepo).addCage(Mockito.any(DbCage.class));

        String result = ctrl.putCage(cageLine);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenAddingExistingCage_putCageReturnsBadRequest() {
        String cageLine = "420 2137 -1";

        /* repo setup */
        when(mockRepo.getNewCageId()).thenReturn(1);
        doThrow(new IllegalArgumentException()).when(mockRepo).addCage(Mockito.any(DbCage.class));

        String result = ctrl.putCage(cageLine);

        assertThat(result).isEqualTo("bad request");
    }


    @Test
    public void whenDeletingExistingCage_deleteReturnsDone() {
        doNothing().when(mockRepo).deleteCage(1);

        String result = ctrl.removeCage(1);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenDeletingNonExistingCage_deleteReturnsNotFound() {
        doThrow(new IllegalArgumentException()).when(mockRepo).deleteCage(1);

        String result = ctrl.removeCage(1);

        assertThat(result).isEqualTo("not found");
    }

    //---------------------------------------- Building Tests ------------------------------------------------------------

    @Test
    public void whenGivenExistingBuilding_getReturnsBuildingToString() {
        DbBuilding building1 = new DbBuilding(1, false,"Barczewska","+48 777 333 465",new ArrayList<>());
        when(mockRepo.findBuilding(1)).thenReturn(Optional.of(building1));
        //System.out.println(building1.toString());
        String result = ctrl.getBuilding(1);

        // IMPORTANT INFORMATION FLOAT FORMATING IN JAVA IS NOT REALLY HELPFUL - CHANGE ',' to '.' HOWEVER U LIKE
        String expected = "DbBuilding: id: 1, address: Barczewska, number: +48 777 333 465, open for zoo: false, number of cages: 0";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenGivenNonExistingBuilding_getReturnsNotFound() {
        when(mockRepo.findBuilding(1)).thenReturn(Optional.empty());

        String result = ctrl.getBuilding(1);

        String expected = "not found";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenAddingBuildingFromString_putConstructsCorrectBuilding() {
        String BuildingLine = "true Pine-Rd/79 +48--902-132-642";
        ArgumentCaptor<DbBuilding> captor = ArgumentCaptor.forClass(DbBuilding.class);

        /* repo setup */
        when(mockRepo.getNewBuildingId()).thenReturn(1);
        doNothing().when(mockRepo).addBuilding(Mockito.any(DbBuilding.class));


        ctrl.putBuilding(BuildingLine);

        verify(mockRepo).addBuilding(captor.capture());
        DbBuilding capturedBuilding = captor.getValue();

        assertThat(capturedBuilding.getId()).isEqualTo(1);
        assertThat(capturedBuilding.getAddress()).isEqualTo("Pine-Rd/79");
        assertThat(capturedBuilding.getPhoneNumber()).isEqualTo("+48--902-132-642");
    }

    @Test
    public void whenAddingNewBuildingFromString_putBuildingReturnsDone() {
        String BuildingLine = "true Pine-Rd/79 +48--902-132-642";

        /* repo setup */
        when(mockRepo.getNewBuildingId()).thenReturn(1);
        doNothing().when(mockRepo).addBuilding(Mockito.any(DbBuilding.class));

        String result = ctrl.putBuilding(BuildingLine);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenAddingExistingBuilding_putBuildingReturnsBadRequest() {
        String BuildingLine = "true Pine-Rd/79 +48--902-132-642";

        /* repo setup */
        when(mockRepo.getNewBuildingId()).thenReturn(1);
        doThrow(new IllegalArgumentException()).when(mockRepo).addBuilding(Mockito.any(DbBuilding.class));

        String result = ctrl.putBuilding(BuildingLine);

        assertThat(result).isEqualTo("bad request");
    }


    @Test
    public void whenDeletingExistingBuilding_deleteReturnsDone() {
        doNothing().when(mockRepo).deleteBuilding(1);

        String result = ctrl.removeBuilding(1);

        assertThat(result).isEqualTo("done");
    }

    @Test
    public void whenDeletingNonExistingBuilding_deleteReturnsNotFound() {
        doThrow(new IllegalArgumentException()).when(mockRepo).deleteBuilding(1);

        String result = ctrl.removeBuilding(1);

        assertThat(result).isEqualTo("not found");
    }
}
