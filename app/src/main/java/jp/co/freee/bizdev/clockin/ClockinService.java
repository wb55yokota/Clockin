package jp.co.freee.bizdev.clockin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.Calendar;

public class ClockinService extends Service {
    static String TAG = ClockinService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String id = "clockin_channel_id";
        String title = "通知のタイトル";
        String description = "通知の内容";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id);
        builder.setContentTitle(title);
        builder.setContentText(description);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        Notification notification = builder.build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(1, notification);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0 ; i<5 ; i++) {
                            Log.d(TAG, "Call");
                            sleepService();
                        }
                        stopForeground(true);
                        Log.d(TAG, "Finish");
                    }
                }

        ).start();
        startForeground(1, notification);
        return START_STICKY;
    }

    public void sleepService() {
        int interval = 6;
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "hour = " + String.valueOf(h));
        if((8 <= h && h <= 12) || 16 <= h && h <= 21) {
            interval = 3;
        }
        try {
            Log.d(TAG, "Sleep " + String.valueOf(interval) + "sec");
            Thread.sleep(interval * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}