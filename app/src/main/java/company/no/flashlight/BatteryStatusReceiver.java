package company.no.flashlight;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryStatusReceiver extends BroadcastReceiver
{

    public int chargingLevel = 0;
    public boolean isCharging;
    public boolean acCharge;
    public boolean usbCharge;

    @Override
    public void onReceive(Context context, Intent intent) {
        chargingLevel=(int)(intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)/(float)intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1)*100);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
        isCharging= status==BatteryManager.BATTERY_STATUS_CHARGING || status== BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);

        usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;


        MainActivity.updateBatteryStatus(chargingLevel,isCharging,acCharge,usbCharge);
    }
}
