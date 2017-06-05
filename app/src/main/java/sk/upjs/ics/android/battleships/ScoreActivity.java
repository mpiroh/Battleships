package sk.upjs.ics.android.battleships;

import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import sk.upjs.ics.android.battleships.provider.ScoreContentProvider;

public class ScoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ScoreCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        getLoaderManager().initLoader(0, Bundle.EMPTY, this);

        ListView scoreListView = (ListView) findViewById(R.id.scoreListView);
        adapter = new ScoreCursorAdapter(this, null);
        scoreListView.setAdapter(adapter);

        scoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScoreActivity.this, DetailActivity.class);
                intent.putExtra("ID", id);
                startActivity(intent);
            }
        });

        scoreListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                new AlertDialog.Builder(ScoreActivity.this)
                        .setTitle("Delete")
                        .setMessage("Do you really want to delete this score?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFromContentProvider(id);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });
    }

    public void deleteFromContentProvider(long id) {
        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                // ...
            }
        };
        Uri uri = ContentUris.withAppendedId(ScoreContentProvider.CONTENT_URI, id);
        handler.startDelete(0, null, uri, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this);
        cursorLoader.setUri(ScoreContentProvider.CONTENT_URI);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.adapter.swapCursor(null);
    }
}
