package jp.co.freee.bizdev.clockin;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiClient {
    private static OkHttpClient client = new OkHttpClient();

    String auth(String url) throws IOException {
        String json = request(url);
        Gson gson = new Gson();
        gson.fromJson(json, String.class);
        return "";
    }

    String request(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();


    }
}
