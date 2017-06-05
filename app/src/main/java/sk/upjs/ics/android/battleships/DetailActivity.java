package sk.upjs.ics.android.battleships;

import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sk.upjs.ics.android.battleships.provider.Provider;
import sk.upjs.ics.android.battleships.provider.ScoreContentProvider;

public class DetailActivity extends AppCompatActivity {
    private long scoreId;
    private TextView detailNick;
    private TextView detailPoints;
    private TextView detailWinner;
    private TextView detailTime;
    private TextView detailDifficulty;
    private TextView detailShotsNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        scoreId = getIntent().getLongExtra("ID", 0);

        detailNick = (TextView) findViewById(R.id.detailNick);
        detailPoints = (TextView) findViewById(R.id.detailPoints);
        detailWinner = (TextView) findViewById(R.id.detailWinner);
        detailTime = (TextView) findViewById(R.id.detailTime);
        detailDifficulty = (TextView) findViewById(R.id.detailDifficulty);
        detailShotsNumber = (TextView) findViewById(R.id.detailShotsNumber);

        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                cursor.moveToFirst();
                detailNick.setText(cursor.getString(cursor.getColumnIndex(Provider.Score.NICK)));
                detailPoints.setText(cursor.getInt(cursor.getColumnIndex(Provider.Score.POINTS)) + " points");
                if (cursor.getInt(cursor.getColumnIndex(Provider.Score.WINNER)) == 0)
                    detailWinner.setText("Loser");
                else
                    detailWinner.setText("Winner");

                long time = cursor.getLong(cursor.getColumnIndex(Provider.Score.TIME)) * 1000L;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                String format = "dd.MM.yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                String dateString = simpleDateFormat.format(cal.getTime());
                detailTime.setText(dateString);
                detailDifficulty.setText("Difficulty: " + cursor.getString(cursor.getColumnIndex(Provider.Score.DIFFICULTY)));
                detailShotsNumber.setText("Number of shots: " + cursor.getInt(cursor.getColumnIndex(Provider.Score.SHOTS_NUMBER)));
            }
        };
        handler.startQuery(0, null, ScoreContentProvider.CONTENT_URI, null, Provider.Score._ID + " = " + scoreId, null, null);
    }
}