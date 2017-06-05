package sk.upjs.ics.android.battleships;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sk.upjs.ics.android.battleships.provider.Provider;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "scoreDatabase";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlTemp = "CREATE TABLE %s (" +
                "%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                "%s TEXT," +
                "%s INT," +
                "%s DATETIME," +
                "%s TEXT," +
                "%s INT," +
                "%s INT" +
                ")";
        String sql = String.format(sqlTemp,
                Provider.Score.TABLE_NAME,
                Provider.Score._ID,
                Provider.Score.NICK,
                Provider.Score.WINNER,
                Provider.Score.TIME,
                Provider.Score.DIFFICULTY,
                Provider.Score.SHOTS_NUMBER,
                Provider.Score.POINTS);
        db.execSQL(sql);

        // sample data
        ContentValues contentValues = new ContentValues();
        contentValues.put(Provider.Score.NICK, "Piťo");
        contentValues.put(Provider.Score.WINNER, 1);
        contentValues.put(Provider.Score.TIME, System.currentTimeMillis() / 1000);
        contentValues.put(Provider.Score.DIFFICULTY, "Normal");
        contentValues.put(Provider.Score.POINTS, 760);
        db.insert(Provider.Score.TABLE_NAME, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(Provider.Score.NICK, "Miro");
        contentValues.put(Provider.Score.WINNER, 0);
        contentValues.put(Provider.Score.TIME, System.currentTimeMillis() / 1000);
        contentValues.put(Provider.Score.DIFFICULTY, "Hard");
        contentValues.put(Provider.Score.POINTS, 260);
        db.insert(Provider.Score.TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
}
