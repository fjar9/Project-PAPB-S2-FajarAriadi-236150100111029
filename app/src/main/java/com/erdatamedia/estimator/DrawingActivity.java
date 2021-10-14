package com.erdatamedia.estimator;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Random;

public class DrawingActivity extends AppCompatActivity {

    private TextView centerTv;
    private ImageView srcImgV;
    private ImageView dstImgV;
    private Random random = new Random(12345);
    private SeekBar seek;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("TAG", "opencv sukses di install");
        } else {
            Log.d("TAG", "opencv gagal install");
        }
    }

    private Mat src = new Mat();
    private Mat dst = new Mat();
    private double cm1 = 0.0325d;
    private double cm2 = 0.065d;
    private double cm3 = 0.0975d;
//    private double cm2 = 0.026458333333333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        centerTv = findViewById(R.id.center);
        seek = findViewById(R.id.seek);
        srcImgV = findViewById(R.id.src);
        dstImgV = findViewById(R.id.dst);

//        String photoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/temp.jpg";
//        srcImgV.setImageBitmap(BitmapFactory.decodeFile(photoPath));

        BitmapDrawable drawable = (BitmapDrawable) srcImgV.getDrawable();
        Bitmap srcBitmap = drawable.getBitmap();

        Utils.bitmapToMat(srcBitmap, src);

        dst = grey(src);

        int halfCols = dst.cols() / 2;
        int halfRows = dst.rows() / 2;

//        Imgproc.line(dst, new Point(halfCols, 0), new Point(halfCols, dst.rows()), randomScalar(), 50);
//        Imgproc.line(dst, new Point(0, halfRows), new Point(dst.cols(), halfRows), randomScalar(), 50);

        Point center = new Point(halfCols, halfRows);
        int radius = 0;
//        Imgproc.circle(dst, center, radius, randomScalar(), 100);

//        for (int row = 0; row < dst.rows(); row++) {
//            for (int col = 0; col < dst.cols(); col++) {
//                dst.get(row, col);
//            }
//        }

        meta(dst);
        dst = rgb(dst);

        Bitmap dstBitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, dstBitmap);

        dstImgV.setImageBitmap(dstBitmap);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Mat dst2 = threshold(dst, progress);

                for (int i = 0; i < dst.rows(); i++) {
                    double[] row = dst2.get(i, halfCols);
                    if (row != null && row.length > 0) {
                        if (row[0] == 255.0 && row[1] == 255.0 && row[2] == 255.0) {
                            Imgproc.circle(dst2, new Point(halfCols, i), radius,
                                    new Scalar(255, 0, 0), 10);
                        }
                    }
                }

                for (int i = 0; i < dst.cols(); i++) {
                    double[] col = dst2.get(halfRows, i);
                    if (col != null && col.length > 0) {
                        if (col[0] == 255.0 && col[1] == 255.0 && col[2] == 255.0) {
                            Imgproc.circle(dst2, new Point(i, halfRows), radius,
                                    new Scalar(255, 0, 0), 10);
                        }
                    }
                }


                Bitmap dstBitmap2 = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dst2, dstBitmap2);

                dstImgV.setImageBitmap(dstBitmap2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void cek(View v) {
        Mat res = new Mat();
        BitmapDrawable drawable = (BitmapDrawable) dstImgV.getDrawable();
        Bitmap srcBitmap = drawable.getBitmap();

        Utils.bitmapToMat(srcBitmap, res);

        int tinggi = 0;
        for (int i = 0; i < res.rows(); i++) {
            double[] row = res.get(i, res.cols() / 2);
//            Log.d("AAA", "AAA " + i + " " + row[0] + " " + row[1] + " " + row[2] + " " + row[3]);
            if (row[0] == 255.0 && row[1] == 0.0 && row[2] == 0.0) {
                tinggi++;
            }
        }

        int panjang = 0;
        for (int i = 0; i < res.cols(); i++) {
            double[] row = res.get(res.rows() / 2, i);
//            Log.d("AAA", "AAA " + i + " " + row[0] + " " + row[1] + " " + row[2] + " " + row[3]);
            if (row[0] == 255.0 && row[1] == 0.0 && row[2] == 0.0) {
                panjang++;
            }
        }

        String s = "panjang badan " + (panjang * cm3) +
                "\ntinggi " + (tinggi * cm3 * 2);
        centerTv.setText(s);

    }

    private Mat grey(Mat src) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        return dst;
    }

    private Mat rgb(Mat src) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_GRAY2BGR);
        return dst;
    }

    private Mat blur(Mat src) {
        Mat dst = new Mat();

        Imgproc.GaussianBlur(src, dst, new Size(3.0d, 3.0d), 2.0d);

        return dst;
    }

    private Mat threshold(Mat src, double i) {
        Mat dst = new Mat();

        Imgproc.threshold(src, dst, i, 255.0d, 0);

        return dst;
    }

    private Scalar randomScalar() {
        return new Scalar(
                (double) random.nextInt(256),
                (double) random.nextInt(256),
                (double) random.nextInt(256)
        );
    }

    private void meta(Mat dst) {
        int halfCols = dst.cols() / 2;
        int halfRows = dst.rows() / 2;

        ArrayList<MatOfPoint> list = new ArrayList<>();
        Mat newa = new Mat();
        Imgproc.findContours(dst, list, newa, 1, 2);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int dpi = displayMetrics.densityDpi;
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        String size = "screen width:" + width +
                "\nscreen height:" + height +
                "\ndpi:" + dpi +
                "\ncols " + dst.cols() + " half " + halfCols +
                "\nrows " + dst.rows() + " half " + halfRows +
                "\nmat of point " + list.size();
        centerTv.setText(size);
    }
}