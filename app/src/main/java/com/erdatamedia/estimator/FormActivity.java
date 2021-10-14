package com.erdatamedia.estimator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImage;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FormActivity extends AppCompatActivity {
    double cm = 0.065d;
    double pb = 0d;
    double ld = 0d;

    private int foto = 0;
    private Bitmap imageBmp;
    private final Random random = new Random(12345);

    private ImageView imageView;
    private ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        findViewById(R.id.bt_close).setOnClickListener(v -> finish());
        findViewById(R.id.bt_save).setOnClickListener(v -> save());
        findViewById(R.id.foto_samping).setOnClickListener(v -> {
            foto = 0;
            CropImage.activity().start(this);
        });
        findViewById(R.id.foto_belakang).setOnClickListener(v -> {
            foto = 1;
            CropImage.activity().start(this);
        });

        findViewById(R.id.hitung).setOnClickListener(v -> bobot());
    }

    private void save() {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                if (resultCode == RESULT_OK) {
                    try {
                        Uri resultUri = result.getUriContent();
                        InputStream imageStream = getContentResolver().openInputStream(resultUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageBmp = selectedImage;
                        if (foto == 0) imageView.setImageURI(resultUri);
                        else imageView2.setImageURI(resultUri);
                        createFile(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void createFile(Bitmap bitmap) {
        File f = new File(getFilesDir(), "foto_" + foto);
        try {
            f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Tools.resizeBitmap(bitmap, 500, 500)
                    .compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            if (foto == 0) convert_panjang_badan();
            else convert_lingkar_dada();

//            if (foto == 0) imageView.setImageBitmap(bitmap);
//            else imageView2.setImageBitmap(bitmap);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void convert_panjang_badan() {
        Mat mat = new Mat();
        Mat mat2 = new Mat();
        Mat mat3 = new Mat();
        Mat mat4 = new Mat();
        Mat mat5 = new Mat();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;
        Bitmap grayBitmap = Bitmap.createBitmap(imageBmp.getWidth(), imageBmp.getHeight(),
                Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(imageBmp, mat, false);
        Imgproc.cvtColor(mat, mat2, 7);
        Imgproc.GaussianBlur(mat2, mat2, new Size(3.0d, 3.0d), 2.0d);
        Mat mat6 = mat2;
        Imgproc.threshold(mat6, mat3, 100.0d, 255.0d, 0);
        Imgproc.Canny(mat6, mat4, 50.0d, 150.0d);
        ArrayList arrayList = new ArrayList();
        Imgproc.findContours(mat4, arrayList, mat5, 1, 2);
        Mat zeros = Mat.zeros(mat4.size(), CvType.CV_8UC3);
        int i = 0;
        while (i < arrayList.size()) {
            Mat mat7 = mat5;
            Imgproc.drawContours(zeros, arrayList, i,
                    new Scalar(random.nextInt(256), random.nextInt(256),
                            random.nextInt(256)), 2, 8, mat7, 0,
                    new Point());
            i++;
            mat5 = mat7;
        }
        Utils.matToBitmap(zeros, grayBitmap);
        double Lwidth = imageBmp.getWidth();
        double Theight = imageBmp.getHeight();
        pb = Lwidth * cm;


        ((EditText) findViewById(R.id.panjang_badan)).setText(String.valueOf(pb));
    }

    private void convert_lingkar_dada() {
        Mat mat = new Mat();
        Mat mat2 = new Mat();
        Mat mat3 = new Mat();
        Mat mat4 = new Mat();
        Mat mat5 = new Mat();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;
        Bitmap grayBitmap = Bitmap.createBitmap(this.imageBmp.getWidth(), this.imageBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(this.imageBmp, mat);
        Imgproc.cvtColor(mat, mat2, 7);
        double d = 2.0d;
        Imgproc.GaussianBlur(mat2, mat2, new Size(3.0d, 3.0d), 2.0d);
        Mat mat6 = mat2;
        Imgproc.threshold(mat6, mat3, 100.0d, 255.0d, 0);
        Imgproc.Canny(mat6, mat4, 50.0d, 150.0d);
        List arrayList = new ArrayList();
        Imgproc.findContours(mat4, arrayList, mat5, 1, 2);
        Mat zeros = Mat.zeros(mat4.size(), CvType.CV_8UC3);
        int i = 0;
        while (i < arrayList.size()) {
            Mat mat7 = mat5;
            Imgproc.drawContours(zeros, arrayList, i, new Scalar((double) this.random.nextInt(256), (double) this.random.nextInt(256), (double) this.random.nextInt(256)), 2, 8, mat7, 0, new Point());
            i++;
            mat5 = mat7;
            d = d;
        }
        double d2 = d;
        Utils.matToBitmap(zeros, grayBitmap);
        double L_width = (double) this.imageBmp.getWidth();
        ld = L_width * this.cm;
        double r = ld / d2;
        double r2 = Math.pow(r, d2);
        double luas = 3.14d * r2;

        ((EditText) findViewById(R.id.lingkar_dada)).setText(String.valueOf(luas));
    }

    private void bobot() {
        double ld2 = Math.pow(ld, 2.0d);
        double bagi = Math.pow(10.0d, 4.0d);
        double bb = ld2 + pb;
        double bbakhir = bb / bagi;
        ((EditText) findViewById(R.id.bobot)).setText(String.valueOf(bbakhir));

    }

}