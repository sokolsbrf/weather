package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private static int LOCATION_PERMISSION_REQUEST_CODE = 777;

    private Button mStartServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        mStartServiceButton = (Button)findViewById(R.id.start_service_button);

        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PermissionChecker.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    Intent serviceIntent = new Intent(MainActivity.this,WeatherService.class);
                    startService(serviceIntent);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0]
                == PermissionChecker.PERMISSION_GRANTED){
            Intent serviceIntent = new Intent(MainActivity.this,WeatherService.class);
            startService(serviceIntent);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
