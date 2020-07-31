package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

import com.di.oximeter.R;
import java.util.Queue;

public class oximeter_activity extends Activity implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private int deviceId, portNum, baudRate;
    private String newline = "\r\n";

    private TextView receiveText,tv_device_connectecd_status;
    private UsbSerialPort usbSerialPort;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;
    private BroadcastReceiver broadcastReceiver;
    private ControlLines controlLines;

    String pulse="",oxigen="";
    Thread timerPulseCheck = null;

    //CircularProgressBar oxigenProgressBar ;
    //CircularProgressBar pluseProgressBar ;
    private LineChart ecgchart;
    private TextView tvspo2,tvbpm,textView8;

    private ImageView ivheart,ivnoheart,iv_backpulse;
    Thread thread;
    int oxmin = 100;
    int oxmax = 0;
    int bpmmin = 200;
    int bpmmax = 0;
    //TextView tv_oxmin ,txdate,txtime,tv_oxmax ,tv_bpmmin,tv_bpmmax;
    ImageView ivsetting;
    LinkedList<Integer> pulsedata = new LinkedList<>();
    LinkedList<Integer> oxygendata = new LinkedList<>();
    Boolean threadsStarted = false;

    Boolean manual_Started = false;
    Boolean manual_Data = false;
    Boolean pulse_Animation_Started = false;
   // MaterialButton start_btn_pulse = null;
   Button start_btn_pulse = null;
    TextView tv_progress;
    Animation Animation1,Animation2,Animation3,Animation4;
    Animation BackAnimation1,BackAnimation2,BackAnimation3,BackAnimation4;
    final Handler progresshandler = new Handler();
    Runnable progresshandlerRunnable= null;
    int progresspulseSet = 30;
    int progresspulse = 30;
    public int checkCountDetectValue = 0;
    public int oldCountDetectValue = -1;

    public oximeter_activity() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.INTENT_ACTION_GRANT_USB)) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }
    void refresh() {
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();

        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++) {
                    //listItems.add(new DevicesFragment.ListItem(device, port, driver));
                    deviceId = device.getDeviceId();//getArguments().getInt("device");
                    portNum = port;//getArguments().getInt("port");
                    baudRate = 115200;//getArguments().getInt("baud");
                }
            } else {
                deviceId = device.getDeviceId();//getArguments().getInt("device");
                portNum = 0;//getArguments().getInt("port");
                baudRate = 115200;//getArguments().getInt("baud");
            }
        }
        String data = Integer.toString(deviceId)+"-"+Integer.toString(portNum);
        //Toast.makeText(this,data,Toast.LENGTH_LONG).show();

    }
    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setHasOptionsMenu(true);
        //setRetainInstance(true);
        //deviceId = getArguments().getInt("device");
        //portNum = getArguments().getInt("port");
        //baudRate = getArguments().getInt("baud");

        deviceId = 1002;//getArguments().getInt("device");
        portNum = 0;//getArguments().getInt("port");
        baudRate = 115200;//getArguments().getInt("baud");
        setContentView(R.layout.oxiboard_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);




        start_btn_pulse =  findViewById(R.id.start_btn_pulse);
        tv_progress = findViewById(R.id.tv_progress);
        tv_device_connectecd_status = findViewById(R.id.tv_device_connectecd_status);
        String data = Integer.toString(deviceId)+"-"+Integer.toString(portNum);
       // Toast.makeText(this,data,Toast.LENGTH_LONG).show();


        //oxigenProgressBar = findViewById(R.id.oxigen);
        //pluseProgressBar = findViewById(R.id.pluse);
        ecgchart = findViewById(R.id.ecgchart);

        ivheart = findViewById(R.id.ivheart);
        iv_backpulse = findViewById(R.id.iv_backpulse);
        ivnoheart = findViewById(R.id.ivheart2);
        //txdate = findViewById(R.id.txdate);
        //txtime = findViewById(R.id.txtime);
        tvspo2 = findViewById(R.id.tv_oxygen);
        tvbpm = findViewById(R.id.tv_bpm);
        //tv_oxmin = findViewById(R.id.oxmin);
        //tv_oxmax = findViewById(R.id.oxmax);
        //tv_bpmmin = findViewById(R.id.bpmmin);
        //tv_bpmmax = findViewById(R.id.bpmmax);
        ivsetting = findViewById(R.id.ivsetting);

        int oxmin = 100;
        int oxmax = 0;
        int bpmmin = 200;
        int bpmmax = 0;


        /*
        // Set Progress
        oxigenProgressBar.setProgress(0f);
        oxigenProgressBar.setProgressMax(100f);
        oxigenProgressBar.setProgressBarColor(BackBone.getInstance().getColor());
        oxigenProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        oxigenProgressBar.setBackgroundProgressBarColorEnd(Color.TRANSPARENT);
        oxigenProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);
        oxigenProgressBar.setProgressBarWidth(5f); // in DP
        oxigenProgressBar.setBackgroundProgressBarWidth(3f); // in DP
        oxigenProgressBar.setRoundBorder(true);
        oxigenProgressBar.setStartAngle(180f);
        oxigenProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_LEFT);

        pluseProgressBar.setProgress(0f);
        pluseProgressBar.setProgressMax(160f);
        pluseProgressBar.setProgressBarColor(BackBone.getInstance().getColor());

        pluseProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

        pluseProgressBar.setBackgroundProgressBarColorEnd(Color.TRANSPARENT);
        pluseProgressBar.setBackgroundProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);
        pluseProgressBar.setProgressBarWidth(5f); // in DP
        pluseProgressBar.setBackgroundProgressBarWidth(3f); // in DP
        pluseProgressBar.setRoundBorder(true);
        pluseProgressBar.setStartAngle(180f);
        pluseProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_LEFT);
        */

        // no description text
        ecgchart.getDescription().setEnabled(false);

        // enable touch gestures
        ecgchart.setTouchEnabled(false);

        ecgchart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        ecgchart.setDragEnabled(true);
        ecgchart.setScaleEnabled(false);
      // moving
        // ecgchart.setDrawGridBackground(false);
        ecgchart.setGridBackgroundColor(R.color.theamcolor);
      //  ecgchart.setHighlightPerDragEnabled(true);
        ecgchart.setBackgroundColor(Color.TRANSPARENT);
        ecgchart.setViewPortOffsets(0f, 0f, 0f, 0f);
        ecgchart.getAxisLeft().setGridColor(R.color.theamcolor);
        ecgchart.getAxisRight().setGridColor(R.color.theamcolor);
        ecgchart.getAxisLeft().setDrawGridLines(false);
         ecgchart.getAxisRight().setDrawGridLines(false);

        ecgchart.getXAxis().setDrawGridLines(false);

         ecgchart.getXAxis().setEnabled(false);
        // set an alternative background color

        // get the legend (only possible after setting data)
        Legend l = ecgchart.getLegend();

        ecgchart.setVisibleXRangeMaximum(10);


        l.setEnabled(false);
        // add empty data
        LineData datapoint = new LineData();
        datapoint.setValueTextColor(BackBone.getInstance().getColor());

        ecgchart.setData(datapoint);


        controlLines = new ControlLines();
        ivsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setting = new Intent(oximeter_activity.this,activity_setting.class);
                oximeter_activity.this.startActivity(setting);
            }
        });
        /*
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manual_Started = !manual_Started;
                if(manual_Started) {
                    start_btn.setBackgroundColor(Color.RED);
                    start_btn.setText("STOP");
                }else{
                    start_btn.setBackgroundColor(Color.GREEN);
                    start_btn.setText("START");
                }
            }
        });*/

        tv_progress.setVisibility(View.INVISIBLE);
        start_btn_pulse.setText("Start");
        start_btn_pulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manual_Started = !manual_Started;
                if(manual_Started) {
                    oximeter_activity.this.oxmin = 100;
                    oximeter_activity.this.oxmax = 0;
                    oximeter_activity.this.bpmmin = 200;
                    oximeter_activity.this.bpmmax = 0;
                    start_btn_pulse.setText("Calculating...");
                    start_btn_pulse.setClickable(true);
                    tv_progress.setText("30 Sec");
                    tv_progress.setVisibility(View.VISIBLE);
                    progresspulse = progresspulseSet;
                    BackBone.getInstance().ResultSPO2 = 0;
                    BackBone.getInstance().ResultPulseRate = 0;
                    startManualProcessing();


                }else{
                    //start_btn_pulse.revertAnimation();


                    pusePulseRate();
                }
            }
        });


       // start_btn_pulse.startAnimation();
        //Animation1,Animation2,Animation3,Animation4;

        startAnimationPulse();
       //
       ivheart.setVisibility(View.GONE);
        iv_backpulse.setVisibility(View.GONE);

        ivnoheart.setVisibility(View.VISIBLE);

        ivheart.startAnimation(Animation1);
        iv_backpulse.setAnimation(BackAnimation1);
    }
    public void pusePulseRate(){
        tv_progress.setVisibility(View.INVISIBLE);
        start_btn_pulse.setText("Start");
        stopPulseAnimation();

        progresshandler.removeCallbacks(progresshandlerRunnable);

    }
    public void startPulseAnimation(){
        pulse_Animation_Started = true;
        ivheart.setVisibility(View.VISIBLE);
        iv_backpulse.setVisibility(View.VISIBLE);
        ivnoheart.setVisibility(View.INVISIBLE);
        ivheart.startAnimation(Animation1);
        iv_backpulse.startAnimation(BackAnimation1);
    }
    public void stopPulseAnimation(){
        pulse_Animation_Started = false;
        ivheart.setVisibility(View.GONE);
        iv_backpulse.setVisibility(View.GONE);
        ivnoheart.setVisibility(View.VISIBLE);
        ivheart.setAnimation(null);
        iv_backpulse.setAnimation(null);
    }
    public void startAnimationPulse(){
        Animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoomanimation1);
        Animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoomanimation2);
        Animation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoomanimation3);
        Animation4 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoomanimation4);

        BackAnimation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.backzoomanimation1);
        BackAnimation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.backzoomanimation2);
        BackAnimation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.backzoomanimation3);
        BackAnimation4 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.backzoomanimation4);

        Animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivheart.startAnimation(Animation2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                ivheart.startAnimation(Animation3);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivheart.startAnimation(Animation4);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try{
                    thread.sleep(200);
                }catch (Exception i){

                }
                ivheart.startAnimation(Animation1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        BackAnimation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_backpulse.startAnimation(BackAnimation2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        BackAnimation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                iv_backpulse.startAnimation(BackAnimation3);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        BackAnimation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_backpulse.startAnimation(BackAnimation4);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        BackAnimation4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try{
                    thread.sleep(200);
                }catch (Exception i){

                }
                iv_backpulse.startAnimation(BackAnimation1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
       // BackBone.getInstance().showMessage(this,"onCreate");
    }
    @Override
    protected void onResume() {
        super.onResume();
       // BackBone.getInstance().showMessage(this,"onResume");

        getstate();
        if(manual_Data) {
            start_btn_pulse.setVisibility(View.VISIBLE);
            pusePulseRate();
        }
        else {
            start_btn_pulse.setVisibility(View.INVISIBLE);
        }


        this.bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_GRANT_USB));
        if(threadsStarted)return ;
        threadsStarted = true;
        refresh();

        if(initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
        if(controlLines != null && connected == Connected.True)
            controlLines.start();




        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread( new Runnable() {

                        @Override
                        public void run() {
                            final int minpulse = 100;
                            final int maxpulse = 120;
                            final int randompulse = new Random().nextInt((maxpulse - minpulse) + 1) + minpulse;

                            final int minoxigen = 92;
                            final int maxoxigen = 95;
                            final int randomoxigen = new Random().nextInt((maxoxigen - minoxigen) + 1) + minoxigen;
                            setProgress( randomoxigen,(float)randompulse);
                        }
                    });

                    try {
                        if(i%10==0)
                            Thread.sleep(5000);
                        else
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
       // thread.start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                addEntry(0);
                for (int i = 0; true; i++) {



                    try {
                        Thread.sleep(250);
                        if(manual_Data )
                            if(!manual_Started)
                                continue;
                        addEntry(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    protected float getRandom(float range, float start) {
        return (float) (Math.random() * range) + start;
    }
    public void startManualProcessing(){
        startPulseAnimation();
        progresshandlerRunnable = new Runnable() {
            public void run() {
                //
                // Do the stuff
                progresshandler.postDelayed(this, 1000);
                progresspulse --;



                tv_progress.setText(Integer.toString(progresspulse)+" Sec" );
                double progresspercentageforcolorchange = progresspulse/(double)progresspulseSet*100.0;
                //tv_progress.setTextColor(Color.GREEN);

                if(progresspulse == 0){
                    generateResultofpulse();
                }

            }
        };
        progresshandlerRunnable.run();



    }
    public void generateResultofpulse(){
        progresshandler.removeCallbacks(progresshandlerRunnable);

        tv_progress.setVisibility(View.INVISIBLE);
        manual_Started = !manual_Started;
        //Intent resultview = new Intent(this, resultview.class);
        //this.startActivity(resultview);

        Intent resultview = new Intent(this, result_detail_view_activity.class);
        this.startActivity(resultview);

    }
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(BackBone.getInstance().getColor());
        set.setLineWidth(1f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setFillAlpha(65);
        set.setFillColor(BackBone.getInstance().getColor());
        set.setDrawCircles(false);
        set.setHighLightColor(BackBone.getInstance().getColor());
        set.setValueTextColor(Color.TRANSPARENT);
        set.setDrawFilled(true);



        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fadeblue);
        set.setFillDrawable(drawable);


        return set;
    }
    private void addEntry(float pulse) {

        LineData data = ecgchart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.addEntry(new Entry(set.getEntryCount(), pulse ), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            ecgchart.notifyDataSetChanged();

            // limit the number of visible entries
            ecgchart.setVisibleXRangeMaximum(60);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            ecgchart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }


    public void setProgress(int progressoxigen,Float progresspluse) {

      //  BackBone.getInstance().showMessage(this,Integer.toString(progressoxigen));
        if(manual_Data )
            if(!manual_Started)
                return;


        float pulseData = progresspluse;
        float oxigenData = progressoxigen;

        if(oxmin > oxigenData && oxigenData!=0){
            oxmin = (int)oxigenData;
        }
        if(oxmax < oxigenData && oxigenData!=0){
            oxmax = (int)oxigenData;
        }
        if(bpmmin > pulseData && oxigenData!=0){
            bpmmin = (int)pulseData;
        }
        if(bpmmax < pulseData && oxigenData!=0){
            bpmmax = (int)pulseData;
        }
        //tv_oxmin.setText(Integer.toString(oxmin)+" %");
        //tv_oxmax.setText(Integer.toString(oxmax)+" %");
        //tv_bpmmin.setText(Integer.toString(bpmmin)+" BPM");
        //tv_bpmmax.setText(Integer.toString(bpmmax)+" BPM");




        int avgstablecount = 5;
        if(pulsedata.size() >avgstablecount)
             pulsedata.removeLast();
        pulsedata.addFirst(Math.round(progresspluse));
        int avgcountsum = 0;
        for(int i =0;i<pulsedata.size();i++){
            avgcountsum += pulsedata.get(i);
        }
        int avgprogresspluse = Math.round(avgcountsum/pulsedata.size());

        if(oxygendata.size() >avgstablecount)
            oxygendata.removeLast();
        oxygendata.addFirst(Math.round(progressoxigen));
        int avgcountsumoxygen = 0;
        for(int i =0;i<oxygendata.size();i++){
            avgcountsumoxygen += oxygendata.get(i);
        }
        progressoxigen = Math.round(avgcountsumoxygen/oxygendata.size());




        long delay = 1000;

        //oxigenProgressBar.setProgressWithAnimation(progressoxigen, delay); // =1s
        //pluseProgressBar.setProgressWithAnimation(avgprogresspluse, delay);

        BackBone.getInstance().ResultPulseRate = avgprogresspluse;
        BackBone.getInstance().ResultSPO2 = progressoxigen;


        addEntry(avgprogresspluse);
        addEntry(avgprogresspluse);

        //txtime.setText("");
        if(progressoxigen > 100)
        {
            progressoxigen = 100;

        }
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String dateToStr = format.format(today);
        //txdate.setText(dateToStr);
        tvspo2.setText(Integer.toString(progressoxigen)+" %");
        tvbpm.setText((Integer.toString(Math.round(avgprogresspluse)))+" BPM");
        checkCountDetectValue ++;
        if(!manual_Data) {
            if(!pulse_Animation_Started) {
                startPulseAnimation();
                if (timerPulseCheck != null) {

                    timerPulseCheck = null;
                }
                timerPulseCheck = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception i) {

                            }

                            if (checkCountDetectValue == oldCountDetectValue ) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        stopPulseAnimation();
                                    }
                                });
                                break;
                            }
                            else{
                                 oldCountDetectValue = checkCountDetectValue;
                            }
                        }

                    }
                });
                timerPulseCheck.start();
            }
        }
    }

    public void heartpulse(){

                ivheart.setVisibility(View.VISIBLE);
                ivnoheart.setVisibility(View.INVISIBLE);



    }
    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        this.stopService(new Intent(this, SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            this.startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !this.isChangingConfigurations())
            service.detach();
        super.onStop();
    }
    /*
    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { this.unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }
*/

    @Override
    public void onPause() {
        this.unregisterReceiver(broadcastReceiver);
        if(controlLines != null)
            controlLines.stop();
        try { this.unbindService(this); } catch(Exception ignored) {}
        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart ){//&& isResumed()) {
            initialStart = false;
            this.runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
    /*
    @Override
    public View onCreate(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oxiboard_activity, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
        TextView sendText = view.findViewById(R.id.send_text);
        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));
        controlLines = new ControlLines(view);
        return view;
    }
    */



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id ==R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void connect() {
        connect(null);
    }

    private void connect(Boolean permissionGranted) {
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            status("connection failed: device not found");
            tv_device_connectecd_status.setText("Device Not Connected");
            tv_device_connectecd_status.setTextColor(Color.RED);
            disconnect();
            return;
        }
        else{
            tv_device_connectecd_status.setText("Device Connected");
            tv_device_connectecd_status.setTextColor(Color.GREEN);
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            status("connection failed: no driver for device");
            disconnect();
            return;
        }
        if(driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            disconnect();
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            disconnect();
            return;
        }

        connected = Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialSocket socket = new SerialSocket(this.getApplicationContext(), usbConnection, usbSerialPort);
            service.connect(socket);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
       // controlLines.stop();
        tv_device_connectecd_status.setText("Device Not Connected");
        tv_device_connectecd_status.setTextColor(Color.RED);
        if(usbSerialPort != null) {
            service.disconnect();
            usbSerialPort = null;
        }
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            byte[] data = (str + newline).getBytes();
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
       // receiveText.append(new String(data));
       // Toast.makeText(this,new String(data),Toast.LENGTH_SHORT).show();



                StringTokenizer dataReceived = new StringTokenizer(new String(data), ",");

                if (dataReceived.hasMoreElements()) {
                    pulse=dataReceived.nextToken();

                }
                if (dataReceived.hasMoreElements()) {

                    oxigen=dataReceived.nextToken();
                }
                // Don't generate garbage runnables inside the loop.
                runOnUiThread( new Runnable() {

                    @Override
                    public void run() {
                        if(pulse.equals("") || oxigen.equals("")){
                            return;
                        }
                        float pulseData = Float.parseFloat(pulse);
                        float oxigenData = Float.parseFloat(oxigen);




                       // final int oxigen = new Random().nextInt((maxoxigen - minoxigen) + 1) + minoxigen;

                        setProgress( (int)oxigenData,pulseData);
                    }
                });


    }


    void status(String str) {
        Toast.makeText(this,str,Toast.LENGTH_LONG).show();
        SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
       // receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");

        connected = Connected.True;
        controlLines.start();
    }

    @Override
    public void onSerialDisConnect() {
        disconnect();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    class ControlLines {
        private static final int refreshInterval = 200; // msec

        private Handler mainLooper;
        private Runnable runnable;

        ControlLines() {
            mainLooper = new Handler(Looper.getMainLooper());
            runnable = this::start; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks

        }

        private void toggle(View v) {
            ToggleButton btn = (ToggleButton) v;
            if (connected != Connected.True) {
                btn.setChecked(!btn.isChecked());
                Toast.makeText(oximeter_activity.this, "not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            String ctrl = "";
            try {
                   ctrl = "RTS"; usbSerialPort.setRTS(true);
                   ctrl = "DTR"; usbSerialPort.setDTR(true);
            } catch (IOException e) {
                status("set" + ctrl + " failed: " + e.getMessage());
            }
        }

        private boolean refresh() {
            String ctrl = "";

            return true;
        }

        void start() {
            if (connected == Connected.True && refresh())
                mainLooper.postDelayed(runnable, refreshInterval);
        }

        void stop() {
            mainLooper.removeCallbacks(runnable);
        }
    }
    public void getstate(){
        SharedPreferences prefs = getSharedPreferences("oximeter", MODE_PRIVATE);
        manual_Data = prefs.getBoolean("ismanual", false);//"No name defined" is the default value.

    }
}
