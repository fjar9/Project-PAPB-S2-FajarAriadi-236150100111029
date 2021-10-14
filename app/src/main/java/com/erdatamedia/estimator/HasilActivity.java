package com.erdatamedia.estimator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class HasilActivity extends AppCompatActivity {
    double badan = 0.0d;
    double bagi;
    double banding = 10.0d;
    double bb;
    double bbakhir;
    double bdn;
    TextView beratbadan;
    double ld2;
    double lebar = 0.0d;
    double luas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hasil);

        this.beratbadan = (TextView) findViewById(R.id.tv_bb);
        Intent intent = getIntent();
        this.badan = intent.getDoubleExtra("data1", this.bdn);
        this.lebar = intent.getDoubleExtra("data", this.luas);
        this.ld2 = Math.pow(this.lebar, 2.0d);
        this.bagi = Math.pow(this.banding, 4.0d);
        this.bb = this.ld2 + this.badan;
        this.bbakhir = this.bb / this.bagi;
        TextView textView = this.beratbadan;
        textView.setText(this.bbakhir + " kg");
    }
}