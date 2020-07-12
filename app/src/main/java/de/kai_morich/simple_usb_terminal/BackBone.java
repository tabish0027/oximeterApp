package de.kai_morich.simple_usb_terminal;

import android.graphics.Color;
import com.di.oximeter.R;
public class BackBone {
    public static BackBone instance = null;

    public int  ResultPulseRate = 0;
    public int  ResultSPO2 = 0;


    public static BackBone getInstance(){
        if(instance == null)
            instance = new BackBone();
        return instance;
    }
    public  int getColor(){

        return Color.rgb(243, 132, 117);
    }
}
