package com.game.rps.controll.game;

import com.game.rps.model.choice.Choice;
import com.game.rps.model.player.Player;

public interface GameController {
    /**
     * Sets the listener for the state change event
     *
     * @param listener Listener for the state change event
     */
    void setListener(GameStateListener listener);

    /**
     * Determines if all components are ready to make the round, if the phone has been shaken,
     * and if the volume key has been pressed.
     *
     * @return true - if all conditions have been fulfilled, false - otherwise
     */
    boolean isReadyToPerformRound();

    /**
     * Performs the round, while conducting all necessary interactions with the other components
     * and also notifies the listener.
     */
    void performRound(Choice opponentChoice);

    /**
     * Gets the current opponent to which the player is connected, null if player isn't connected
     */
    Player getOpponent();

    /**
     * Sets the current opponent to which the player is connected
     *
     * @param player Current opponent
     */
    void setOpponent(Player player);

    /**
     * Carries out a state change operation
     *
     * @param state New state to change
     */
    void switchState(State state);

    /**
     * Returns current player choice, null if there is no choice
     *
     * @return player choice
     */
    Choice getChoice();

    /**
     * Returns the current state of the controller
     *
     * @return the current state of the controller
     */
    State getState();

    /**
     * Restarts the current controller
     */
    void restart();
}
