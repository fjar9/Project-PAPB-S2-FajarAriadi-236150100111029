package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.erdatamedia.estimator.drawing.SimpleDrawingView;

public class TestingActivity extends AppCompatActivity {

    private Float jarak = 2f;
    private Float acuan = 1f;

    private View acuanV;
    private TextView acuanTv;
    private ImageView fotoImg;
    private TextView realTv;
    private SeekBar seekBar;
    private TextView jarakTv;
    private TextView screenTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        LinearLayout view = findViewById(R.id.drawing);

        acuanV = findViewById(R.id.acuan);
        acuanTv = findViewById(R.id.acuanm);
        fotoImg = findViewById(R.id.foto);
        realTv = findViewById(R.id.real);

        String acuan = "1m\nwidth:" + acuanV.getLayoutParams().width +
                "\nheigth:" + acuanV.getLayoutParams().height;
        acuanTv.setText(acuan);

        String real = "1m\nwidth:" + fotoImg.getLayoutParams().width +
                "\nheigth:" + fotoImg.getLayoutParams().height;
        realTv.setText(real);

        jarakTv = findViewById(R.id.jarak);
        seekBar = findViewById(R.id.seek);

        int wdt = fotoImg.getLayoutParams().width;
        int hgt = fotoImg.getLayoutParams().height;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                jarakTv.setText(progress + "m");
                acuanV.setLayoutParams(new RelativeLayout.LayoutParams(
                        10 * progress,
                        10 * progress));
                String acuan = "1m\nwidth:" + acuanV.getLayoutParams().width +
                        "\nheigth:" + acuanV.getLayoutParams().height;
                acuanTv.setText(acuan);

                String real = progress + "m\nwidth:" + fotoImg.getLayoutParams().width +
                        "\nheigth:" + fotoImg.getLayoutParams().height;
                realTv.setText(real);

                fotoImg.setLayoutParams(new RelativeLayout.LayoutParams(
                        progress + wdt,
                        progress + hgt));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        screenTv = findViewById(R.id.screen);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        String size = "height:" + height + "\nwidth:" + width;
        screenTv.setText(size);
    }
}