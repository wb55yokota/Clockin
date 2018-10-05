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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.freee.bizdev.clockin.models.Attendances;
import jp.co.freee.bizdev.clockin.models.ClockData;

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
    Attendances mAttendances;

    private int mRetryCount = 0;

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

        mAttendances = Attendances.load(getApplicationContext());
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), id);

        NotificationManager manager = (NotificationManager) getSystemService(getApplication().NOTIFICATION_SERVICE);
        if (manager.getNotificationChannel(id) == null) {
            NotificationChannel channel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }

        Notification notification = mBuilder
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_freee_swallow_white)
            .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManagerCompat.from(getApplicationContext()).notify(NOTIFICATION_ID, notification);

        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    for(int i=0 ; i<999 ; i++) {  // TODO: 実稼働時はwhileループに
                        updateCountAndStatus();
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

    private void updateCountAndStatus() {
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
        boolean isChangeClock = false;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(toStatus == Status.WORKING && getInt(R.integer.clock_in_start_hour) <= h && h <= getInt(R.integer.clock_in_end_hour)) {
            // 出勤検知
            mHistories.add(0, "出勤 : " + timestamp);
            isChangeClock = mAttendances.setClock(ClockData.Clock.IN, cal);
        }
        if(toStatus == Status.PRIVATE && getInt(R.integer.clock_out_start_hour) <= h && h <= getInt(R.integer.clock_out_end_hour)) {
            // 退勤検知
            mHistories.add(0, "退勤 : " + timestamp);
            isChangeClock = mAttendances.setClock(ClockData.Clock.OUT, cal);
        }
        if(isChangeClock) {
            mAttendances.save(getApplicationContext());
            mRetryCount = 0;
            ApiClient.ClockType clockType = null;
            if(toStatus == Status.WORKING) {
                clockType = ApiClient.ClockType.IN;
            } else {
                clockType = ApiClient.ClockType.OUT;
            }
            sendClockToApi(clockType, timestamp.substring(0, 10));
        }
        mCurrentStatus = toStatus;
        mCount = 0;
    }

    private void sendClockToApi(final ApiClient.ClockType clockType, final String dateString) {
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    if (!(new ApiClient(getApplicationContext()).clock(clockType, dateString))) {
                        if (++mRetryCount < 5) {
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sendClockToApi(clockType, dateString);
                        }
                    }
                }
            }
        ).start();
    }

    public void sleepService() {
        int interval = getInt(R.integer.interval_long);
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "hour = " + String.valueOf(h));
        if((getInt(R.integer.clock_in_start_hour) <= h && h <= getInt(R.integer.clock_in_end_hour)) ||
                getInt(R.integer.clock_out_start_hour) <= h && h <= getInt(R.integer.clock_out_end_hour)) {
            interval = getInt(R.integer.interval_short);
        }
        try {
            Log.d(TAG, "Sleep " + String.valueOf(interval) + "sec");
            Thread.sleep(interval * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int getInt(int id) {
        return getApplicationContext().getResources().getInteger(id);
    }
}
