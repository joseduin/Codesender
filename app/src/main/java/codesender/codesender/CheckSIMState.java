package codesender.codesender;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by Jose on 21/12/2016.
 */

public class CheckSIMState extends BroadcastReceiver {

    private static final String TAG = "CheckSIMState";
    private int notificationId = 123;
    private NotificationManager notificationManager;
    private SimReader simReader;
    private SharedPreferences sharedPreferencesDefault;
    private SharedPreferences sharedPreferencesPrivate;

    @Override
    public void onReceive(Context context, Intent intent) {
        simReader = new SimReader(context);
        sharedPreferencesPrivate = context.getSharedPreferences("first_time", Context.MODE_PRIVATE);
        sharedPreferencesDefault = PreferenceManager.getDefaultSharedPreferences(context);
        String simSerial = sharedPreferencesPrivate.getString("simcardserial","");
        if(SimReader.isSIMChanged(simReader, sharedPreferencesPrivate)){

            if(!simReader.emptyPhoneNumber()) {
                simReader.sendPhoneNumberToServerBg(sharedPreferencesDefault);
                sharedPreferencesPrivate.edit().
                        putString("simcardserial",simReader.getSimSerialNumber()).apply();
                Log.d(TAG, "cambio simCard" + simReader.getSimSerialNumber());

            } else {
                // Se crea Modal
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setContentTitle("CodeSender");
                mBuilder.setContentText("You must set your new phone number");
                mBuilder.setTicker("Alert: SIM card has been changed !");
                mBuilder.setSmallIcon(R.mipmap.codesender);
                //mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                mBuilder.setLights(Color.RED, 3000, 3000);
                mBuilder.setAutoCancel(true);

                Intent resultIntent = new Intent(context,MainActivity.class);

                PendingIntent resultPendingIntent = PendingIntent.
                        getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                mBuilder.setContentIntent(resultPendingIntent);

                notificationManager = (NotificationManager) context.
                        getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(notificationId, mBuilder.build());
            }

        }

    }
}
