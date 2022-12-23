package com.game.rps.controll.game;

import com.game.rps.model.choice.Choice;

class GameLogicImpl implements GameLogic {

    @Override
    public State judge(Choice choiceOfFirstPlayer, Choice choiceOfSecondPlayer) {
        if (choiceOfFirstPlayer == Choice.ROCK && choiceOfSecondPlayer == Choice.PAPER)
            return State.LOSE;
        if (choiceOfFirstPlayer == Choice.ROCK && choiceOfSecondPlayer == Choice.SCISSORS)
            return State.WIN;
        if (choiceOfFirstPlayer == Choice.SCISSORS && choiceOfSecondPlayer == Choice.ROCK)
            return State.LOSE;
        if (choiceOfFirstPlayer == Choice.SCISSORS && choiceOfSecondPlayer == Choice.PAPER)
            return State.WIN;
        if (choiceOfFirstPlayer == Choice.PAPER && choiceOfSecondPlayer == Choice.ROCK)
            return State.WIN;
        if (choiceOfFirstPlayer == Choice.PAPER && choiceOfSecondPlayer == Choice.SCISSORS)
            return State.LOSE;
        return State.DRAW;
    }
}
