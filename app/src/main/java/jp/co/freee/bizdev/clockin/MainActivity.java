package jp.co.freee.bizdev.clockin;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.google.gson.Gson;

import jp.co.freee.bizdev.clockin.models.Attendances;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startClockinService();
        findViewById(R.id.showButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAndShowAttendances();
            }
        });
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAttendances();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // permission確認
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, 0);
        }
        loadAndShowAttendances();
    }

    private void startClockinService() {
        Intent intent = getServiceIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopClockinService() {
        stopService(getServiceIntent());
    }

    private Intent getServiceIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Intent(this, ClockinForegroundService.class);
        } else {
            return new Intent(this, ClockinService.class);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            stopClockinService();
            this.finish();
        }
    }

    private void loadAndShowAttendances() {
        Attendances attendances = Attendances.load(getApplicationContext());
        AppCompatTextView textView = findViewById(R.id.showTextView);
        Gson gson = new Gson();
        textView.setText(CommonLibs.prettyJson(gson.toJson(attendances)));
    }

    private void clearAttendances() {
        SharedPreferences preferences = CommonLibs.getSharedPreferences(getApplicationContext());
        preferences.edit().clear().commit();
        loadAndShowAttendances();
        stopClockinService();
//        startClockinService();
    }
}
