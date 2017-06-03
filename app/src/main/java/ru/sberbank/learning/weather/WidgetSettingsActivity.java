package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

public class WidgetSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 13);
        } else {
            setResult(RESULT_OK);
            finish();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setResult(RESULT_OK, new Intent());
            finish();
        } else {
            // TODO: message
            setResult(RESULT_CANCELED);
        }
    }
}
