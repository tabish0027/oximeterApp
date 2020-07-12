package com.di.oximeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import com.di.oximeter.R;
import de.kai_morich.simple_usb_terminal.oximeter_activity;

public class Splash extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        new Handler().postDelayed (new Runnable() {
            @Override
            public void run() {
                Intent mainboardClass = new Intent(Splash.this, oximeter_activity.class);
                Splash.this.startActivity(mainboardClass);

                finish ();
            }
        }, 3000);
    }




    @Override
    protected void onResume() {
        super.onResume();


    }
}
