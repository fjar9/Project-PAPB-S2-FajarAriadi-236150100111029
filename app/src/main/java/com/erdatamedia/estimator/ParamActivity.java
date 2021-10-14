package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.erdatamedia.estimator.networking.NetworkService;
import com.erdatamedia.estimator.networking.ResponseDashboard;
import com.erdatamedia.estimator.networking.RetrofitClientInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParamActivity extends AppCompatActivity {
    public NetworkService service = RetrofitClientInstance.getRetrofitInstance()
            .create(NetworkService.class);
    private Dialog loading;
    private EditText editText;
    private Activity activity = ParamActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param);
        loading = loadingDialog();

        editText = findViewById(R.id.param);
        findViewById(R.id.btn).setOnClickListener(v -> {
                    loading.show();
                    service.update_param(editText.getText().toString())
                            .enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body()) {
                                            Toast.makeText(activity, "Sukses", Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(activity, "Fail", Toast.LENGTH_SHORT).show();
                                    }
                                    loading.dismiss();
                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {
                                    Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }
                            });
                }
        );


    }

    @Override
    protected void onStart() {
        super.onStart();
        loading.show();
        service.dashboard().enqueue(new Callback<ResponseDashboard>() {
            @Override
            public void onResponse(Call<ResponseDashboard> call, Response<ResponseDashboard> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().status) {
                        editText.setText(response.body().param + "");
                    }
                } else {
                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseDashboard> call, Throwable t) {
                loading.dismiss();

            }
        });
    }

    public Dialog loadingDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setDimAmount(0f);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}