package com.tq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tq.Utils.ViewUtils;
import com.tq.view.QQHealthView;

public class MainActivity extends AppCompatActivity {

    private QQHealthView mQQHealthView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQQHealthView= (QQHealthView) findViewById(R.id.qqHealthView);

        mQQHealthView.setSteps(new int[]{100,528,11,121,134,212,143});
        mQQHealthView.setThemeColor(ViewUtils.getThemeColorPrimary(MainActivity.this));
    }
}
