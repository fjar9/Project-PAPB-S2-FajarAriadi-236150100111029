package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.erdatamedia.estimator.networking.EstimasiResponse;
import com.erdatamedia.estimator.networking.NetworkService;
import com.erdatamedia.estimator.networking.ResponseDashboard;
import com.erdatamedia.estimator.networking.RetrofitClientInstance;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessActivity extends AppCompatActivity {
    public NetworkService service = RetrofitClientInstance.getRetrofitInstance()
            .create(NetworkService.class);
    private ImageView preview;
    private Dialog loading;
    private Activity activity = ProcessActivity.this;

    private ArrayAdapter<String> bangsaAdapter;
    private AutoCompleteTextView bangsaEt, fisiologisEt, kelaminEt;
    private List<ResponseDashboard.Race> races = new ArrayList<>();
    private List<String> racenames = new ArrayList<>();
    private List<String> fisiologis = new ArrayList<>();
    private List<String> gender = new ArrayList<>();
    private TextView panjangBadanTv, lingkarDadaTv, resultTv;
    private Bitmap bitmap;
    private Random random = new Random(12345);
    private double cm = 0.065d;
//    private double cm = 0.0264583d;

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
        setContentView(R.layout.activity_process);
        loading = loadingDialog();

        preview = findViewById(R.id.imageView);
        bangsaEt = findViewById(R.id.et_bangsa);
        fisiologisEt = findViewById(R.id.et_fisiologis);
        kelaminEt = findViewById(R.id.et_kelamin);
        panjangBadanTv = findViewById(R.id.panjang_badan);
        lingkarDadaTv = findViewById(R.id.lingkar_dada);
        resultTv = findViewById(R.id.result);

        String photoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/temp.jpg";
        bitmap = BitmapFactory.decodeFile(photoPath);
        preview.setImageBitmap(bitmap);

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

        findViewById(R.id.normal).setOnClickListener(v -> preview.setImageBitmap(bitmap));
        findViewById(R.id.prepro).setOnClickListener(v -> first());

    }

    private void estimate() {
        loading.show();
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("panjang_badan", panjangBadanTv.getText().toString());
        fieldMap.put("lingkar_dada", lingkarDadaTv.getText().toString());
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
                            BitmapDrawable drawable = (BitmapDrawable) preview.getDrawable();
                            Bitmap srcBitmap = drawable.getBitmap();

                            Mat prev = new Mat();
                            Utils.bitmapToMat(srcBitmap, prev);

                            String result = response.body().datas +
                                    " kg\nwidth " + prev.rows() +
                                    " \nheigth " + prev.cols();
                            resultTv.setText(result);
                        } else
                            Toast.makeText(activity, response.body().msg, Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
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
    public void onStart() {
        super.onStart();
        loading.show();
        service.dashboard()
                .enqueue(new Callback<ResponseDashboard>() {
                    @Override
                    public void onResponse(Call<ResponseDashboard> call, Response<ResponseDashboard> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().status) {
                                races.clear();
                                races.addAll(response.body().datas.races);
                                for (ResponseDashboard.Race race : races)
                                    racenames.add(race.race_name);
                                bangsaAdapter.notifyDataSetChanged();

                                first();
                            }
                        } else {
                            Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseDashboard> call, Throwable t) {
                        loading.dismiss();
                        Toast.makeText(activity, "Periksa koneksi anda", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void first() {
        Mat src = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(bitmap, src);

        Imgproc.cvtColor(src, dst, 7);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(dst, bitmap);

        preview.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> second(dst), 1000);
    }

    private void second(Mat src) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(src, dst, new Size(3.0d, 3.0d), 2.0d);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        preview.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> third(dst), 1000);
    }

    private void third(Mat src) {
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, 100.0d, 255.0d, 0);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        preview.setImageBitmap(bitmap);

//        new Handler().postDelayed(() -> fourth(dst), 1000);
        new Handler().postDelayed(() -> {

            double pb = bitmap.getWidth() * cm;
            double ld = bitmap.getHeight() * cm;

            panjangBadanTv.setText(String.valueOf(pb));
            lingkarDadaTv.setText(String.valueOf(ld));

            preview.setImageBitmap(bitmap);
            loading.dismiss();
        }, 1000);
    }

    private void fourth(Mat src) {
        Mat dst = new Mat();
        Imgproc.Canny(src, dst, 50.0d, 150.0d);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        preview.setImageBitmap(bitmap);

//        new Handler().postDelayed(() -> fifth(dst), 1000);

    }

    private void fifth(Mat src) {
        ArrayList<MatOfPoint> list = new ArrayList<>();
        Mat dst = new Mat();
        Imgproc.findContours(src, list, dst, 1, 2);
        Mat zeros = Mat.zeros(src.size(), CvType.CV_8UC3);

        int i = 0;
        while (i < list.size()) {
            Mat mat7 = dst;
            Imgproc.drawContours(zeros, list, i,
                    new Scalar(
                            (double) random.nextInt(256),
                            (double) random.nextInt(256),
                            (double) random.nextInt(256)
                    ), 2, 8, mat7, 0, new Point());
            i++;
            dst = mat7;
        }

        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(zeros, bitmap);

        new Handler().postDelayed(() -> {

            double pb = bitmap.getWidth() * cm;
            double ld = bitmap.getHeight() * cm;

            panjangBadanTv.setText(String.valueOf(pb));
            lingkarDadaTv.setText(String.valueOf(ld));

            preview.setImageBitmap(bitmap);
            loading.dismiss();
        }, 1000);
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