package jp.co.freee.bizdev.clockin.models;

// e.g.
//  { clockIn: "09:45:12", clockOut: "19:12:43" }
public class ClockData {
    public String clockIn;   // e.g. 09:45:12
    public String clockOut;  // e.g. 19.12:38

    public enum Clock {
        IN,
        OUT;
    }

    public ClockData(String clockIn, String clockOut) {
        this.clockIn = clockIn;
        this.clockOut = clockOut;
    }
}
