package sk.upjs.ics.android.battleships;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onNewGameButtonClick(View view) {
        Intent intent = new Intent(this, ArrangeActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // prevents activity from saving to back stack
        startActivity(intent);
    }

    public void onScoreButtonClick(View view) {
        Intent intent = new Intent(this, ScoreActivity.class);
        startActivity(intent);
    }

    public void onSettingsButtonClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
