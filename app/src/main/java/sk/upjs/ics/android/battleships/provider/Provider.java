package sk.upjs.ics.android.battleships.provider;

import android.provider.BaseColumns;

public interface Provider {
    public interface Score extends BaseColumns {
        // nepridavame _ID, pridalo sa to automaticky lebo dedime od BaseColums
        public static final String TABLE_NAME = "score";
        public static final String NICK = "nick";
        public static final String WINNER = "winner";
        public static final String TIME = "time";
        public static final String DIFFICULTY = "difficulty";
        public static final String SHOTS_NUMBER = "shotsNumber";
        public static final String POINTS = "points";
    }
}
