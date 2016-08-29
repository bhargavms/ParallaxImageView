package com.bhargavms.sample.parallaximageview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bhargavms.parallaximageview.ParallaxImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParallaxImageView para = (ParallaxImageView) findViewById(R.id.para);
        int[] drawableRes = new int[]{
                R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4, R.drawable.p5
        };
        para.setDrawables(drawableRes);
    }
}
