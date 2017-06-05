package sk.upjs.ics.android.battleships;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import sk.upjs.ics.android.battleships.provider.Provider;

public class ScoreCursorAdapter extends CursorAdapter {
    private final LayoutInflater inflater;

    public ScoreCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.score_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long time = cursor.getLong(cursor.getColumnIndex(Provider.Score.TIME)) * 1000L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        String format = "dd.MM.yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        String dateString = simpleDateFormat.format(cal.getTime());

        ((TextView) view.findViewById(R.id.timeTextView)).setText(dateString);
        ((TextView) view.findViewById(R.id.nickTextView)).setText(cursor.getString(
                cursor.getColumnIndex(Provider.Score.NICK)));
        ((TextView) view.findViewById(R.id.pointsTextView)).setText(
                cursor.getString(cursor.getColumnIndex(Provider.Score.POINTS)));
    }
}
