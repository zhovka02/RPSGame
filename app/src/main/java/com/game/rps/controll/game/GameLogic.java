package com.game.rps.controll.game;

import com.game.rps.model.choice.Choice;

interface GameLogic {
    /**
     * Determines the winner among two players by the rules of rock-paper-scissors
     *
     * @param choiceOfFirstPlayer  Choice of the first player (current player)
     * @param choiceOfSecondPlayer Choice of the second player (opponent)
     * @return State.WIN or State.LOSE or State.DRAW which corresponds to the outcome
     * of the round for the first player
     */

    State judge(Choice choiceOfFirstPlayer, Choice choiceOfSecondPlayer);
}
