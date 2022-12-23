package com.game.rps.controll.game;

import com.game.rps.model.choice.Choice;

import org.junit.Assert;
import org.junit.Test;

public class GameLogicTest {
    @Test
    public void testOfGameLogic(){
        GameLogic logic = new GameLogicImpl();
        // Rock > Scissors
        Assert.assertEquals(State.WIN, logic.judge(Choice.ROCK, Choice.SCISSORS));
        // Rock = Rock
        Assert.assertEquals(State.DRAW, logic.judge(Choice.ROCK, Choice.ROCK));
        // Rock < Paper
        Assert.assertEquals(State.LOSE, logic.judge(Choice.ROCK, Choice.PAPER));
        // Scissors > Paper
        Assert.assertEquals(State.WIN, logic.judge(Choice.SCISSORS, Choice.PAPER));
        // Scissors = Scissors
        Assert.assertEquals(State.DRAW, logic.judge(Choice.SCISSORS, Choice.SCISSORS));
        // Scissors < Rock
        Assert.assertEquals(State.LOSE, logic.judge(Choice.SCISSORS, Choice.ROCK));
        // Paper > Rock
        Assert.assertEquals(State.WIN, logic.judge(Choice.PAPER, Choice.ROCK));
        // Paper = Paper
        Assert.assertEquals(State.DRAW, logic.judge(Choice.PAPER, Choice.PAPER));
        // Paper < Scissors
        Assert.assertEquals(State.LOSE, logic.judge(Choice.PAPER, Choice.SCISSORS));
    }
}
