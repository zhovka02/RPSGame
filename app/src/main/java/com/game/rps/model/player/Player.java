package com.game.rps.model.player;

public interface Player {

    /**
     * Gets current name of Player
     *
     * @return Name of Player
     */
    String getName();

    /**
     * Updates current Player's name
     *
     * @param name Name to set
     */
    void setName(String name);

    /**
     * Gets current Player's number of wins
     *
     * @return Player's number of wins
     */
    int getWins();

    /**
     * Updates current Player's number of wins
     *
     * @param wins Player's number of wins
     */
    void setWins(int wins);

    /**
     * Gets current Player's number of losses
     *
     * @return Player's number of wins
     */
    int getLosses();

    /**
     * Updates current Player's number of losses
     *
     * @param losses Player's number of losses
     */
    void setLosses(int losses);

    /**
     * Gets current Player's number of rounds
     *
     * @return Player's number of wins
     */
    int getRounds();

    /**
     * Updates current Player's number of rounds
     *
     * @param rounds Player's number of rounds
     */
    void setRounds(int rounds);
}
