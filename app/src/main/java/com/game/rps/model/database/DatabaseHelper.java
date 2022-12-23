package com.game.rps.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.game.rps.model.player.Player;
import com.game.rps.model.player.PlayerImpl;


class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Player_DBe.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_PLAYER_ENTRIES =
            "CREATE TABLE " + DatabaseContract.PlayerEntry.TABLE_NAME + " (" +
                    DatabaseContract.PlayerEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.PlayerEntry.COLUMN_NAME_PLAYER_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.PlayerEntry.COLUMN_NAME_WIN + " INTEGER" + COMMA_SEP +
                    DatabaseContract.PlayerEntry.COLUMN_NAME_LOSS + " INTEGER" + COMMA_SEP +
                    DatabaseContract.PlayerEntry.COLUMN_NAME_ROUNDS + " INTEGER" + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseContract.PlayerEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void savePlayer(SQLiteDatabase aDb, Player player) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_PLAYER_ID, player.getName());
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_WIN, 0);
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_LOSS, 0);
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_ROUNDS, 0);
        aDb.insert(
                DatabaseContract.PlayerEntry.TABLE_NAME,
                DatabaseContract.PlayerEntry.COLUMN_NAME_NULLABLE,
                values);
    }

    public void updatePlayer(SQLiteDatabase aDb, Player player) {
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_PLAYER_ID, player.getName());
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_WIN, player.getWins());
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_LOSS, player.getLosses());
        values.put(DatabaseContract.PlayerEntry.COLUMN_NAME_ROUNDS, player.getRounds());

        aDb.update(DatabaseContract.PlayerEntry.TABLE_NAME, values,
                DatabaseContract.PlayerEntry._ID + "=?",
                new String[]{"1"});
    }

    public PlayerImpl getPlayer(SQLiteDatabase aDb) {
        String theQuery = "SELECT " + DatabaseContract.PlayerEntry.COLUMN_NAME_PLAYER_ID + ","
                + DatabaseContract.PlayerEntry.COLUMN_NAME_WIN + ","
                + DatabaseContract.PlayerEntry.COLUMN_NAME_LOSS + ","
                + DatabaseContract.PlayerEntry.COLUMN_NAME_ROUNDS
                + " FROM "
                + DatabaseContract.PlayerEntry.TABLE_NAME;
        Cursor c = aDb.rawQuery(theQuery, null);
        if (c.getCount() == 0) {
            PlayerImpl player = new PlayerImpl("user", 0, 0, 0);
            this.savePlayer(aDb, player);
            c.close();
            return player;
        } else {
            c.moveToNext();
            String name = c.getString(0);
            int wins = c.getInt(1);
            int loses = c.getInt(2);
            int rounds = c.getInt(3);
            PlayerImpl player = new PlayerImpl(name, wins, loses, rounds);
            this.savePlayer(aDb, player);
            c.close();
            return player;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase aDb) {
        aDb.execSQL(SQL_CREATE_PLAYER_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_PLAYER_ENTRIES);
    }
}

