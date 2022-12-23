package com.game.rps.model.database;

import com.game.rps.model.player.Player;


public interface RPSDatabase {
    /**
     * Saves information about the player in the local storage
     *
     * @param player the player for whom the information must be saved
     */
    void savePlayer(Player player);

    /**
     * Updates information about the player in the local storage
     *
     * @param player the player for whom the information must be updated
     */
    void updatePlayer(Player player);

    /**
     * Gets information about the player from the local storage
     *
     * @return player instance with all information about current device's player from local storage
     */
    Player getPlayer();

    void close();
}

