package com.game.rps.model.database;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.game.rps.model.player.Player;
import com.game.rps.model.player.PlayerImpl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {

    private Context context;
    private RPSDatabase database;

    @Before
    public void setUp() {
        this.context = ApplicationProvider.getApplicationContext();
        this.database = RPSDatabaseImpl.getInstance(this.context, true);
    }

    @After
    public void tearUp(){
        this.context = null;
        this.database.close();
    }

    @Test
    public void testSavePlayer() {
        Player playerToPut = new PlayerImpl("PLayerToPut", 0, 0, 0);
        this.database.savePlayer(playerToPut);
        Player playerFromDB = this.database.getPlayer();
        Assert.assertEquals(playerToPut.getName(), playerFromDB.getName());
        Assert.assertEquals(playerToPut.getLosses(), playerFromDB.getLosses());
        Assert.assertEquals(playerToPut.getRounds(), playerFromDB.getRounds());
        Assert.assertEquals(playerToPut.getWins(), playerFromDB.getWins());
    }

    @Test
    public void testSaveAndUpdatePlayer() {
        Player playerToPut = new PlayerImpl("PLayerToPut", 0, 0, 0);
        this.database.savePlayer(playerToPut);
        playerToPut.setWins(10);
        playerToPut.setLosses(10);
        playerToPut.setRounds(20);
        this.database.updatePlayer(playerToPut);
        Player playerFromDB = this.database.getPlayer();
        Assert.assertEquals(playerToPut.getName(), playerFromDB.getName());
        Assert.assertEquals(playerToPut.getLosses(), playerFromDB.getLosses());
        Assert.assertEquals(playerToPut.getRounds(), playerFromDB.getRounds());
        Assert.assertEquals(playerToPut.getWins(), playerFromDB.getWins());
    }

    @Test
    public void testSaveAndUpdatePlayerWithNameChange() {
        Player playerToPut = new PlayerImpl("PLayerToPut", 0, 0, 0);
        this.database.savePlayer(playerToPut);
        playerToPut.setWins(10);
        playerToPut.setLosses(10);
        playerToPut.setRounds(20);
        playerToPut.setName("NewName");
        this.database.updatePlayer(playerToPut);
        Player playerFromDB = this.database.getPlayer();
        Assert.assertEquals(playerToPut.getName(), playerFromDB.getName());
        Assert.assertEquals(playerToPut.getLosses(), playerFromDB.getLosses());
        Assert.assertEquals(playerToPut.getRounds(), playerFromDB.getRounds());
        Assert.assertEquals(playerToPut.getWins(), playerFromDB.getWins());
    }
}
