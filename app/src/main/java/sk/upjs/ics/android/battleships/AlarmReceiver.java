package sk.upjs.ics.android.battleships;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, GameActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Battleships")
                .setContentText("You have not finish your game yet.")
                .setSmallIcon(R.drawable.app_ship_icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        Notification n = builder.build();
        n.flags = n.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        nm.notify(0, n);
    }
}
