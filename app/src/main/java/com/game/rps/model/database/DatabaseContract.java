package com.game.rps.model.database;

import android.provider.BaseColumns;

final class DatabaseContract
{

    /* Inner class that defines the table contents */
    protected static abstract class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "user_table";
        public static final String COLUMN_NAME_PLAYER_ID = "userid";
        public static final String COLUMN_NAME_WIN = "win";
        public static final String COLUMN_NAME_LOSS = "loss";
        public static final String COLUMN_NAME_ROUNDS = "rounds";
        public static final String COLUMN_NAME_NULLABLE = null;
    }

}