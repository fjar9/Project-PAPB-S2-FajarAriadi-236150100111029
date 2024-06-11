package com.erdatamedia.estimator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.erdatamedia.estimator.networking.EstimasiResponse;
import com.erdatamedia.estimator.networking.NetworkService;
import com.erdatamedia.estimator.networking.ResponseDashboard;
import com.erdatamedia.estimator.networking.RetrofitClientInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrepareActivity extends AppCompatActivity {
    public NetworkService service = RetrofitClientInstance.getRetrofitInstance()
            .create(NetworkService.class);
    private Dialog loading;

    private ImageView ldImg, pbImg;
    private ArrayAdapter<String> bangsaAdapter;
    private AutoCompleteTextView bangsaEt, fisiologisEt, kelaminEt;
    private List<ResponseDashboard.Race> races = new ArrayList<>();
    private List<String> racenames = new ArrayList<>();
    private List<String> fisiologis = new ArrayList<>();
    private List<String> gender = new ArrayList<>();
    private TextView ldTv, pbTv, resultTv;
    private Activity activity = PrepareActivity.this;
    private Integer param = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare);
        loading = loadingDialog();

        ldImg = findViewById(R.id.ldImg);
        pbImg = findViewById(R.id.pbImg);
        findViewById(R.id.ldCapture).setOnClickListener(v ->
                startActivity(new Intent(activity, RecognitionActivity.class)
                        .putExtra("SIDE", "BACK")
                        .putExtra("PARAM", param)
                ));
        findViewById(R.id.pbCapture).setOnClickListener(v ->
                startActivity(new Intent(activity, RecognitionActivity.class)
                        .putExtra("SIDE", "SIDE")
                        .putExtra("PARAM", param)
                ));

        bangsaEt = findViewById(R.id.et_bangsa);
        fisiologisEt = findViewById(R.id.et_fisiologis);
        kelaminEt = findViewById(R.id.et_kelamin);
        ldTv = findViewById(R.id.ldTv);
        pbTv = findViewById(R.id.pbTv);
        resultTv = findViewById(R.id.result);

        bangsaAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, racenames);
        bangsaEt.setAdapter(bangsaAdapter);

        fisiologis.add("Pedet");
        fisiologis.add("Muda");
        fisiologis.add("Dewasa");
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, fisiologis);
        fisiologisEt.setAdapter(adapter2);

        gender.add("Jantan");
        gender.add("Betina");
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, gender);
        kelaminEt.setAdapter(adapter3);

        findViewById(R.id.bt_calculate).setOnClickListener(v -> {
            if (!TextUtils.isEmpty(bangsaEt.getText()) && !TextUtils.isEmpty(fisiologisEt.getText())
                    && !TextUtils.isEmpty(kelaminEt.getText())) estimate();
            else Toast.makeText(activity, "Lengkapi form", Toast.LENGTH_SHORT).show();
        });


    }

    private void estimate() {
        loading.show();
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("lingkar_dada", ldTv.getText().toString().replace(",", "."));
        fieldMap.put("panjang_badan", pbTv.getText().toString().replace(",", "."));
        String bangsa = String.valueOf(bangsaEt.getText());
        fieldMap.put("id_race", races.get(racenames.indexOf(bangsa)).id_race);
        fieldMap.put("fisiologis", String.valueOf(fisiologisEt.getText()).toLowerCase());
        String gender = String.valueOf(kelaminEt.getText());
        fieldMap.put("gender", !gender.equals("Jantan") ? "1" : "0");

        service.estimate(fieldMap).enqueue(new Callback<EstimasiResponse>() {
            @Override
            public void onResponse(Call<EstimasiResponse> call, Response<EstimasiResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().status) {
                            String result = response.body().datas + " kg";
                            resultTv.setText(result);
                            param = response.body().param;
                        } else {
                            Toast.makeText(activity, response.body().msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<EstimasiResponse> call, Throwable t) {
                Toast.makeText(activity, "Periksa koneksi anda", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/CHOSEN") + "/";
        File[] files = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/CHOSEN/")
                .listFiles();

        long newest_back = 0;
        long newest_side = 0;
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains("BACK")) {
                    newest_back = Math.max(file.lastModified(), newest_back);
                }

                if (file.getName().contains("SIDE")) {
                    newest_side = Math.max(file.lastModified(), newest_side);
                }
            }

            for (File file : files) {
                String[] meta = file.getName().split("_");
                if (file.getName().contains("BACK")) {
                    if (file.lastModified() < newest_back) {
                        file.delete();
                    } else {
                        ldTv.setText(
                                String.format(Locale.getDefault(), "%.01f",
                                        Float.parseFloat(meta[3].replace(".jpg", "")))
                        );
                        ldImg.setImageBitmap(BitmapFactory.decodeFile(path + file.getName()));
                    }
                }

                if (file.getName().contains("SIDE")) {
                    if (file.lastModified() < newest_side) {
                        file.delete();
                    } else {
                        pbTv.setText(
                                String.format(Locale.getDefault(), "%.01f",
                                        Float.parseFloat(meta[3].replace(".jpg", "")))
                        );
                        pbImg.setImageBitmap(BitmapFactory.decodeFile(path + file.getName()));
                    }
                }
            }


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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