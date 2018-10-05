package jp.co.freee.bizdev.clockin;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    public enum ClockType {
        IN("clock_in"),
        OUT("clock_out");

        private String value;

        ClockType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }
    private static OkHttpClient client = new OkHttpClient();
    private Context mContext;

    public ApiClient(Context context) {
        mContext = context;
    }

    public String auth(String url) {
        String json = request(url, "{}");
        Gson gson = new Gson();
        gson.fromJson(json, String.class);
        return "";
    }

    public boolean clock(ClockType clockType, String date) {
        JSONObject json = new JSONObject();
        String jsonString = "";
        try {
            json.put("company_id", mContext.getResources().getInteger(R.integer.company_id));
            json.put("type", clockType.toString());
            json.put("base_date", date);
            jsonString = json.toString();
            String resultJsonString = request(getClocksUri(), jsonString);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String request(String url, String jsonParams) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonParams);
        Request request = new Request.Builder()
            .method("POST", body)
            .url(url)
            .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("{\"error\": { \"code\": 500, \"message\": \"%s\"}", e.getMessage());
        }
    }

    private String getClocksUri() {
        return String.format(
            "%s://%s/hr/api/v1/employees/%d/time_clocks",
            mContext.getResources().getString(R.string.api_protocol),
            mContext.getResources().getString(R.string.api_host),
            mContext.getResources().getInteger(R.integer.employees_id));
    }
}
