package company.no.flashlight;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Flash flash;
    private static RelativeLayout base;
    private static TextView batteryP;
    private static TextView status;
    private static Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flash =new Flash(this);

        base = (RelativeLayout) findViewById(R.id.base_layout);
        batteryP = (TextView) findViewById(R.id.textView);
        status = (TextView) findViewById(R.id.status);

        BatteryStatusReceiver batt = new BatteryStatusReceiver();
        IntentFilter batteryIntent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent bintent = this.registerReceiver(batt,batteryIntent);

        SwitchCompat switchCompat= (SwitchCompat) findViewById(R.id.Switch);
        switchCompat.setOnCheckedChangeListener(this);
        switchCompat.setChecked(true);

    }

    private void TouchHandler() {
        if(flash.IsInitiated()) {
            if (!flash.getFlashStatus()) {
                flash.turnOnFlashLight();
            } else {
                flash.turnOffFlashLight();
            }
        }
        else {
            try {
                flash.init();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!flash.getFlashStatus()&& flash.IsInitiated()){
            flash.release();
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(flash.IsInitiated()){
            flash.release();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.Switch:
                if(!isChecked){
                    TouchHandler();
                    Toast.makeText (this,"Err Switch is off!!",Toast.LENGTH_SHORT).show ();
                }else{
                    TouchHandler();
                    Toast.makeText (this,"Yes Switch is on!!", Toast.LENGTH_SHORT).show ();

                }
                break;
            default:
                break;
        }

    }

    private static void startAnimation(int colorfro,int colorto) {
        ObjectAnimator colorAnimator = ObjectAnimator.ofObject(base,"backgroundColor",new ArgbEvaluator(),colorfro,colorto);
        colorAnimator.setDuration(800);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.start();
    }


    public static void updateBatteryStatus(int chargingLevel, boolean isCharging, boolean acCharge, boolean usbCharge)
    {
        batteryP.setText(Integer.toString(chargingLevel)+"%");

        if(isCharging)
        {
            if(acCharge)
                status.setText("AC Plugged in");
            else
                status.setText("USB Plugged in");
        }
        else
        {
            status.setText("Discharging");
        }

        /*if(chargingLevel<20)
        {
            int colorfro =((ColorDrawable)base.getBackground()).getColor();
            startAnimation(colorfro,Color.parseColor(String.valueOf(R.color.batteryLow)));
            toolbar.setBackgroundColor(Color.parseColor(String.valueOf(R.color.batteryLowTool)));
        }
        else
        {
            int colorfro =((ColorDrawable)base.getBackground()).getColor();
            //startAnimation(colorfro, ContextCompat.getR(R.color.batteryOk)));
            toolbar.setBackgroundColor(Color.parseColor(String.valueOf(R.color.batteryOkTool)));
        }*/

    }
}
