package com.erdatamedia.estimator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;


public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 10;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "opencv sukses di install");
        } else {
            Log.d("TAG", "opencv gagal install");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.param).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ParamActivity.class)));
        findViewById(R.id.start).setOnClickListener(v -> {
            if (hasPermissions()) {
                Intent intent = new Intent(this, PrepareActivity.class);
                startActivity(intent);
            } else requestPermission();
        });
    }

    private boolean hasPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                REQUEST_CODE
        );
    }
}