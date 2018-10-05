package jp.co.freee.bizdev.clockin.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.freee.bizdev.clockin.ClockinService;
import jp.co.freee.bizdev.clockin.CommonLibs;

// e.g.
//  [
//    { "2018-10-01": { clockIn: "09:45:12", clockOut: "19:12:43" } }
//    { "2018-10-02": { clockIn: "09:45:12", clockOut: "19:12:43" } }
//    ...
//  ]
public class Attendances {
    static String TAG = ClockinService.class.getSimpleName();

    public List<Attendance> attendances;

    private static final String SP_KEY = "attendances";

    public Attendances(List<Attendance> att) {
        attendances = att;
        if(attendances == null) {
            attendances = new ArrayList<Attendance>();
        }
    }

    public static Attendances load(Context context) {
        SharedPreferences preferences = CommonLibs.getSharedPreferences(context);
        Gson gson = new Gson();
        String s = preferences.getString(SP_KEY, "");
        Attendances attendances = gson.fromJson(s, Attendances.class);
        if(attendances == null) {
            attendances = new Attendances(null);
        }
        return attendances;
    }

    public void save(Context context) {
        SharedPreferences preferences = CommonLibs.getSharedPreferences(context);
        Gson gson = new Gson();
        preferences.edit().putString(SP_KEY, gson.toJson(this)).commit();
    }

    public boolean setClock(ClockData.Clock clock, Calendar cal) {
        boolean changeClock = false;

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf1.format(cal.getTime());

        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String time = sdf2.format(cal.getTime());

        if(hasDate(date)) {
            Log.d(TAG, "has date : " + date);
            Attendance attendance = getAttendance(date);
            if(clock == ClockData.Clock.IN && attendance.clockData.clockIn == null) {
                attendance.clockData.clockIn = time;
                changeClock = true;
            }
            if(clock == ClockData.Clock.OUT && attendance.clockData.clockOut == null) {
                attendance.clockData.clockOut = time;
                changeClock = true;
            }
        } else {
            Log.d(TAG, "not has date : " + date);
            ClockData clockData = new ClockData(null, null);
            if(clock == ClockData.Clock.IN) {
                clockData.clockIn = time;
            }
            if(clock == ClockData.Clock.OUT) {
                clockData.clockOut = time;
            }
            attendances.add(new Attendance(date, clockData));
            changeClock = true;
        }
        return changeClock;
    }

    public boolean hasDate(String date) {
        boolean exists = false;
        for(Attendance attendance : attendances) {
            if (attendance.date.equals(date)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public Attendance getAttendance(String date) {
        for(Attendance attendance : attendances) {
            if (attendance.date.equals(date)) {
                return attendance;
            }
        }
        return null;
    }

}
