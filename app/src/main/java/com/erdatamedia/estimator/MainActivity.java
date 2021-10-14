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

        initToolbar();
        initComponent();

        findViewById(R.id.param).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ParamActivity.class)));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Estimator Bobot Sapi");
        Tools.setSystemBarColor(this, R.color.blue_600);
    }

    private void initComponent() {
        Tools.displayImageOriginal(this, findViewById(R.id.image_1), R.drawable.banner);
        Tools.displayImageOriginal(this, findViewById(R.id.image_2), R.drawable.sapi);

        findViewById(R.id.s1).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Tes1Activity.class)
                        .putExtra("SKENARIO", 1))
        );
        findViewById(R.id.s2).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Tes1Activity.class)
                        .putExtra("SKENARIO", 2))
        );
        findViewById(R.id.s3).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Tes1Activity.class)
                        .putExtra("SKENARIO", 3))
        );
        findViewById(R.id.s4).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Tes1Activity.class)
                        .putExtra("SKENARIO", 4))
        );
        findViewById(R.id.s5).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, Tes1Activity.class)
                        .putExtra("SKENARIO", 5))
        );
        findViewById(R.id.testing1).setOnClickListener(v -> {
            if (hasPermissions()) enableCamera();
            else requestPermission();
        });

        findViewById(R.id.testing2).setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawingActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.testing3).setOnClickListener(v -> {
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

    private void enableCamera() {
        Intent intent = new Intent(this, TakePictureActivity.class);
        startActivity(intent);
    }
}