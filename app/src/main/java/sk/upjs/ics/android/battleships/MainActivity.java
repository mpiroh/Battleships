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






/*
https://stackoverflow.com/questions/12507665/android-set-notification-after-3-hours
https://www.youtube.com/watch?v=EnG5ZIVfki8
https://stackoverflow.com/questions/12358485/android-open-activity-without-save-into-the-stack
https://stackoverflow.com/questions/4047683/android-how-to-resume-an-app-from-a-notification
https://www.youtube.com/watch?v=tFilQ48HR08&list=PLiWr-Oy5PNkXH5wQfBdX_gDnVW8xgykrB
 */
