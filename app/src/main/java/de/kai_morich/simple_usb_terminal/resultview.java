package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.di.oximeter.R;

import androidx.annotation.Nullable;

public class resultview extends Activity {
    TextView tv_result_pulse,tv_result_spo2;
    ImageView iv_backbutton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        tv_result_spo2 = findViewById(R.id.tv_result_spo2);
        tv_result_pulse = findViewById(R.id.tv_result_pulse_rate);

        iv_backbutton = findViewById(R.id.iv_backbutton);
        iv_backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultview.this.finish();
            }
        });
        iv_backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultview.this.finish();
            }
        });
        tv_result_spo2.setText( Integer.toString(BackBone.getInstance().ResultSPO2) + "%");
        tv_result_pulse.setText( Integer.toString(BackBone.getInstance().ResultPulseRate) + " bps");

    }

}
