package com.erdatamedia.estimator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.erdatamedia.estimator.networking.NetworkService;
import com.erdatamedia.estimator.networking.RetrofitClientInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpalshActivity extends AppCompatActivity {
    public NetworkService service = RetrofitClientInstance.getRetrofitInstance()
            .create(NetworkService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);


        new Handler().postDelayed(this::next, 2000);
    }

    private void checkUpdate() {
        service.check(BuildConfig.APPLICATION_ID)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            if (!response.body().equals(BuildConfig.VERSION_NAME)) confirmUpdate();
                            else next();
                        } else next();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        next();
                    }
                });
    }

    private void confirmUpdate() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Perhatian")
                .setMessage("Pembaruan aplikasi tersedia")
                .setPositiveButton("Perbarui", (dialog, which) -> {
                    String appName = BuildConfig.APPLICATION_ID;
                    startActivity(new Intent("android.intent.action.VIEW",
                            Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                })
                .setNegativeButton("Nanti saja", (dialog, which) -> next())
                .setCancelable(false)
                .show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.green_800));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.grey_800));
    }

    private void next() {
        Intent i = new Intent(SpalshActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}