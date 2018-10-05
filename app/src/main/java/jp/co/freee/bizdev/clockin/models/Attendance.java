package jp.co.freee.bizdev.clockin.models;

// e.g.
//  { "2018-10-01": { clockIn: "09:45:12", clockOut: "19:12:43" } }
public class Attendance {
    public String date;
    public ClockData clockData;

    public Attendance(String date, ClockData clockData) {
        this.date = date;
        this.clockData = clockData;
    }
}
