package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.di.oximeter.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class result_detail_view_activity extends Activity implements OnHisteryItemClickListener{
    RecyclerView rv_old_history;
    TextView tv_bpm,tv_oxygen;
    ArrayList<OldHisteryModel> oldhistory = new ArrayList();
    ArrayList<String> oldhistoryString = new ArrayList();
    TextView tv_daily,tv_weekly,tv_clear_histery;
    ImageView iv_backbutton;
    HisteryResultAdapter adapter = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_detail_view_activity);
        rv_old_history= findViewById(R.id.rv_old_history);
        tv_bpm= findViewById(R.id.tv_bpm);
        tv_oxygen= findViewById(R.id.tv_oxygen);
        tv_daily= findViewById(R.id.tv_daily);
        tv_weekly= findViewById(R.id.tv_weekly);
        tv_clear_histery = findViewById(R.id.tv_clear_histery);
        iv_backbutton = findViewById(R.id.iv_backbutton);
        tv_daily.setVisibility(View.GONE);
        tv_weekly.setVisibility(View.GONE);


        adapter = new HisteryResultAdapter(this,this);

        rv_old_history.setLayoutManager(new LinearLayoutManager(this));
        rv_old_history.setAdapter(adapter);
        adapter.addAll(oldhistory);
        adapter.notifyDataSetChanged();
        tv_clear_histery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearHistery();
            }
        });
        tv_daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDaily();
            }
        });
        tv_weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectWeekly();
            }
        });
        iv_backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result_detail_view_activity.this.finish();
            }
        });
        tv_oxygen.setText( Integer.toString(BackBone.getInstance().ResultSPO2) + " %");
        tv_bpm.setText( Integer.toString(BackBone.getInstance().ResultPulseRate) + " Bpm");
        AddData(BackBone.getInstance().ResultPulseRate,BackBone.getInstance().ResultSPO2);

    }
    public void selectDaily(){
        tv_daily.setBackgroundResource(R.drawable.bg_result_round_border_old_select);
        tv_weekly.setBackgroundResource(R.drawable.bg_result_round_border_old_unselect);

    }
    public void selectWeekly(){
        tv_daily.setBackgroundResource(R.drawable.bg_result_round_border_old_unselect);
        tv_weekly.setBackgroundResource(R.drawable.bg_result_round_border_old_select);
    }
    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onHisteryItemClickListener(OldHisteryModel oldHisteryModel) {
        BackBone.getInstance().ReviewPulseRate = oldHisteryModel.getHeartpulse();
        BackBone.getInstance().ReviewSPO2 = oldHisteryModel.getOxygen();

        Intent resultview = new Intent(this, resultview.class);
        this.startActivity(resultview);
    }
    public void clearHistery(){
        oldhistoryString.clear();
        oldhistory.clear();
        saveData();
        loadData();
    }
    public void AddData(int Pulse, int oxigen){
        loadData();
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        String newData = Integer.toString(Pulse) + "," +Integer.toString(oxigen)+ "," +date;
        oldhistoryString.add(0,newData);
        saveData();
        loadData();
    }
    public void saveData(){
        TinyDB tinydb = new TinyDB(getApplicationContext());
        tinydb.putListString("saveData", oldhistoryString);

    }
    public void loadData(){
        TinyDB tinydb = new TinyDB(getApplicationContext());
        oldhistory.clear();
        oldhistoryString = tinydb.getListString("saveData");
        for(int i=0;i<oldhistoryString.size();i++){
            String[] Token = oldhistoryString.get(i).split(",");
            oldhistory.add(new OldHisteryModel(Token[0],Token[1],Token[2]));
        }
        adapter.addAll(oldhistory);
    }

}
