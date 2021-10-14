package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.canhub.cropper.CropImage;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tes1Activity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ImageView imageView5;
    private ImageView imageView6;
    private Bitmap imageBmp;

    double Lwidth;
    double Theight;
    double cm = 0.065d;
    double pb;
    private Random random = new Random(12345);

    private int scenario = 1;

    static {
        if (OpenCVLoader.initDebug()) Log.d("TAG", "opencv sukses di install");
        else Log.d("TAG", "opencv gagal install");
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_tes1);
        scenario = getIntent().getIntExtra("SKENARIO", 1);

        progressBar = findViewById(R.id.progressBar);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView4 = findViewById(R.id.imageView4);
        imageView5 = findViewById(R.id.imageView5);
        imageView6 = findViewById(R.id.imageView6);

        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            CropImage.activity().start(this);
        });
        findViewById(R.id.btn_convert).setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            imageView2.setImageBitmap(null);
            imageView3.setImageBitmap(null);
            imageView4.setImageBitmap(null);
            imageView5.setImageBitmap(null);
            imageView6.setImageBitmap(null);
            new Handler().postDelayed(this::first, 0);
        });

        findViewById(R.id.btn_next).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Tes2Activity.class);
//            intent.putExtra("pb", pb);
            intent.putExtra("data", pb);
            startActivity(intent);
        });
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
                        imageBmp = BitmapFactory.decodeStream(imageStream);
                        imageView1.setImageBitmap(imageBmp);
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

    private void first() {
        Mat src = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(imageBmp, src);

        Imgproc.cvtColor(src, dst, 7);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(dst, bitmap);

        imageView2.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> {
            if (scenario == 1) {
                fourth(dst);
            }
            if (scenario == 2) {
                second(dst);
            }
            if (scenario == 3) {
                third(dst);
            }
            if (scenario == 4) {
                fourth(dst);
            }
            if (scenario == 5) {
                second(dst);
            }
        }, 0);
    }

    private void second(Mat src) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(src, dst, new Size(3.0d, 3.0d), 2.0d);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        imageView3.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> {
            if (scenario == 1) {
                fifth(dst);
            }
            if (scenario == 2) {
                fourth(dst);
            }
            if (scenario == 5) {
                third(dst);
            }
        }, 0);
    }

    private void third(Mat src) {
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, 100.0d, 255.0d, 0);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        imageView4.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> {
            if (scenario == 3) {
                fourth(dst);
            }
            if (scenario == 4) {
                fifth(dst);
            }
            if (scenario == 5) {
                fourth(dst);
            }
        }, 0);
    }

    private void fourth(Mat src) {
        Mat dst = new Mat();
        Imgproc.Canny(src, dst, 50.0d, 150.0d);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        imageView5.setImageBitmap(bitmap);

        new Handler().postDelayed(() -> {
            if (scenario == 1) {
                second(dst);
            }
            if (scenario == 2) {
                fifth(dst);
            }
            if (scenario == 3) {
                fifth(dst);
            }
            if (scenario == 4) {
                third(dst);
            }
            if (scenario == 5) {
                fifth(dst);
            }
        }, 0);
    }

    private void fifth(Mat src) {
        ArrayList list = new ArrayList();
        Mat dst = new Mat();
        Imgproc.findContours(src, list, dst, 1, 2);
        Mat zeros = Mat.zeros(src.size(), CvType.CV_8UC3);

        int i = 0;
        while (i < list.size()) {
            Mat mat7 = dst;
            Imgproc.drawContours(zeros,
                    list, i,
                    new Scalar(
                            (double) this.random.nextInt(256),
                            (double) this.random.nextInt(256),
                            (double) this.random.nextInt(256)
                    ), 2, 8, mat7, 0, new Point());
            i++;
            dst = mat7;
        }

        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(zeros, bitmap);

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            imageView6.setImageBitmap(bitmap);

//            Toast.makeText(Tes1Activity.this,
//                    imageBmp.getWidth() + " * " + cm + " = " + (imageBmp.getWidth() * cm)
//                    , Toast.LENGTH_SHORT).show();
        }, 0);
    }

    public void convert() {
        Mat mat = new Mat();
        Mat mat2 = new Mat();
        Mat mat3 = new Mat();
        Mat mat4 = new Mat();
        Mat mat5 = new Mat();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;

        Bitmap cannyBitmap = Bitmap.createBitmap(imageBmp.getWidth(), imageBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(imageBmp, mat);
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
            Imgproc.drawContours(zeros,
                    arrayList, i,
                    new Scalar(
                            (double) this.random.nextInt(256),
                            (double) this.random.nextInt(256),
                            (double) this.random.nextInt(256)
                    ), 2, 8, mat7, 0, new Point());
            i++;
            mat5 = mat7;
        }
        Utils.matToBitmap(zeros, cannyBitmap);
        this.Lwidth = (double) imageBmp.getWidth();
        this.Theight = (double) imageBmp.getHeight();
        this.pb = this.Lwidth * this.cm;

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            imageView6.setImageBitmap(cannyBitmap);

            Toast.makeText(Tes1Activity.this,
                    Lwidth + " * " + cm + " = " + (pb)
                    , Toast.LENGTH_SHORT).show();
        }, 0);
    }
}