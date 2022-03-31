package com.bennyplo.designgraphicswithopenglphongreflection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private MyView GLView;
    private View mControlsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        GLView =new MyView(this);
        //setContentView(R.layout.activity_fullscreen);
        setContentView(GLView);
        //set full screen
        mControlsView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // if this is enabled, the navigator bar will show on tablet .. phone is fine though
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        mControlsView.setSystemUiVisibility(uiOptions);
        ////////////////////////

    }
}
