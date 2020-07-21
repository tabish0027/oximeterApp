package de.kai_morich.simple_usb_terminal;

import android.widget.TextView;

public class OldHisteryModel {
    public String  heartpulse;
    public String  oxygen;
    public String  datetime;


    public OldHisteryModel(String heartpulse, String oxygen, String datetime) {
        this.heartpulse = heartpulse;
        this.oxygen = oxygen;
        this.datetime = datetime;

    }

    public String getHeartpulse() {
        return heartpulse;
    }

    public void setHeartpulse(String heartpulse) {
        this.heartpulse = heartpulse;
    }

    public String getOxygen() {
        return oxygen;
    }

    public void setOxygen(String oxygen) {
        this.oxygen = oxygen;
    }

}
