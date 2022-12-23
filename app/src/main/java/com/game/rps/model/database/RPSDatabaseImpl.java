package com.game.rps.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.game.rps.model.player.Player;
import com.game.rps.model.player.PlayerImpl;

public class RPSDatabaseImpl implements RPSDatabase {

    private static DatabaseHelper helper;
    private static SQLiteDatabase database;
    private static RPSDatabase accessor;

    private RPSDatabaseImpl() {
        // hide constructor
    }

    public static RPSDatabase getInstance(Context context, boolean newInstance) {
        if (helper == null || newInstance) {
            helper = new DatabaseHelper(context);
            database = helper.getWritableDatabase();
            RPSDatabaseImpl.accessor = new RPSDatabaseImpl();
        }
        return accessor;
    }

    public static RPSDatabase getInstance(Context context) {
        if (helper == null) {
            helper = new DatabaseHelper(context);
            database = helper.getWritableDatabase();
            RPSDatabaseImpl.accessor = new RPSDatabaseImpl();
        }
        return accessor;
    }

    public static RPSDatabase getInstance() {
        return accessor;
    }


    @Override
    public void savePlayer(Player player) {
        if (helper != null)
            helper.savePlayer(database, player);
    }

    @Override
    public void updatePlayer(Player player) {
        if (helper != null)
            helper.updatePlayer(database, player);
    }

    @Override
    public Player getPlayer() {
        if (helper != null)
            return helper.getPlayer(database);
        return new PlayerImpl("fake", 0, 0, 0);
    }

    @Override
    public void close() {
        database.close();
    }
}
