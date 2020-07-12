package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.di.oximeter.R;
import androidx.annotation.Nullable;

public class activity_setting extends Activity {
    Switch sw_manual,sw_auto;
    ImageView iv_backbutton;
    Boolean check = false;
    CompoundButton.OnCheckedChangeListener listioner_auto = null;
    CompoundButton.OnCheckedChangeListener listioner_manual = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sw_manual = findViewById(R.id.sw_manual);
        sw_auto = findViewById(R.id.sw_auto);
        sw_manual.setChecked(getstate());
        sw_auto.setChecked(!getstate());
        iv_backbutton = findViewById(R.id.iv_backbutton);
        iv_backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity_setting.this.finish();
            }
        });



        listioner_auto = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_manual.setOnCheckedChangeListener(null);
                sw_manual.setChecked(!isChecked);
                savestate(false);
                sw_manual.setOnCheckedChangeListener(listioner_manual);

            }
        };
        listioner_manual = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sw_auto.setOnCheckedChangeListener(null);
                sw_auto.setChecked(!isChecked);
                savestate(true);
                sw_auto.setOnCheckedChangeListener(listioner_auto);
            }
        };
        sw_auto.setOnCheckedChangeListener(listioner_auto);
        sw_manual.setOnCheckedChangeListener(listioner_manual);

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
