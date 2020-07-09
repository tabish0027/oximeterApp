package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.di.oximeter.R;
import androidx.annotation.Nullable;

public class activity_setting extends Activity {
    Switch sw_manual,sw_auto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sw_manual = findViewById(R.id.sw_manual);
        sw_auto = findViewById(R.id.sw_auto);
        sw_manual.setChecked(getstate());
        sw_auto.setChecked(!getstate());
        sw_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_manual.setChecked(!isChecked);
                savestate(false);
            }
        });
        sw_manual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_auto.setChecked(!isChecked);
                savestate(true);
            }
        });
    }
    public void savestate(Boolean state){
        SharedPreferences.Editor editor = getSharedPreferences("oximeter", MODE_PRIVATE).edit();
        editor.putBoolean("ismanual", state);
        editor.apply();
    }
    public Boolean getstate(){
        SharedPreferences prefs = getSharedPreferences("oximeter", MODE_PRIVATE);
        return prefs.getBoolean("ismanual", false);//"No name defined" is the default value.

    }
}
