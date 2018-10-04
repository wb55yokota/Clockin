package jp.co.freee.bizdev.clockin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.O)
public class ClockinForegroundService extends Service {
    static String TAG = ClockinService.class.getSimpleName();

    public enum Status {
        WORKING,
        PRIVATE;
    }

    Status mCurrentStatus = Status.PRIVATE;
    int mCount = 0;
    List<String> mHistories = new ArrayList<>();

    final int NOTIFICATION_ID = 1;
    final String id = "clockin_channel_id";

    NotificationCompat.Builder mBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = "通知のタイトル";
        String description = "通知の内容";

        mBuilder = new NotificationCompat.Builder(getApplicationContext(), id);

        NotificationManager manager = (NotificationManager) getSystemService(getApplication().NOTIFICATION_SERVICE);
        if (manager.getNotificationChannel(id) == null) {
            NotificationChannel channel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }

        Notification notification = mBuilder
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setVibrate(new long[]{0L})
                .setDefaults(0)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManagerCompat.from(getApplicationContext()).notify(NOTIFICATION_ID, notification);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0 ; i<50 ; i++) {
                            Log.d(TAG, "Call");
                            String message = "";
                            if(CommonLibs.isInsideFreee(getApplicationContext())) {
                                if(mCurrentStatus == Status.PRIVATE) {
                                    if(++mCount >= 3) {
                                        changeStatusTo(Status.WORKING);
                                    }
                                } else {
                                    mCount = 0;
                                }
                                message = "inside Freee : " + String.valueOf(mCount);
                            } else {
                                if(mCurrentStatus == Status.WORKING) {
                                    if(++mCount >= 3) {
                                        changeStatusTo(Status.PRIVATE);
                                    }
                                } else {
                                    mCount = 0;
                                }
                                message = "outside Freee : " + String.valueOf(mCount);
                            }
                            updateContentText(message);
                            sleepService();
                        }
                        stopForeground(Service.STOP_FOREGROUND_DETACH);
                        NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                        Log.d(TAG, "Finish");
                    }
                }

        ).start();
        startForeground(1, notification);
        return START_STICKY;
    }

    private void updateContentText(String message) {
        for(String history : mHistories) {
            message = message.concat("/");
            message = message.concat(history);
        }
        mBuilder.setContentText(message);
        NotificationManagerCompat.from(getApplicationContext()).notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void changeStatusTo(Status toStatus) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(cal.getTime());
        if(toStatus == Status.WORKING) {
            // 出勤検知
            mHistories.add(0, "出勤 : " + timestamp);
        } else {
            // 退勤検知
            mHistories.add(0, "退勤 : " + timestamp);
        }
        mCurrentStatus = toStatus;
        mCount = 0;
    }

    public void sleepService() {
        int interval = 10;
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "hour = " + String.valueOf(h));
        if((8 <= h && h <= 12) || 16 <= h && h <= 21) {
            interval = 5;
        }
        try {
            Log.d(TAG, "Sleep " + String.valueOf(interval) + "sec");
            Thread.sleep(interval * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}