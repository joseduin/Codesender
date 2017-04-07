package codesender.codesender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncomingSms extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";

    // SimReader
    private SimReader simReader;

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // All Shared Preferences Keys
    public static final String KEY_REGEX = "regex";

    private ArrayList<String> list_expressions = new ArrayList<>();

    public IncomingSms() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    simReader = new SimReader(context);

                    setExpresions(context);
                    searchCode(messages[0].getMessageBody(), context);
                }
            }
        }
    }

    private void setExpresions(Context context) {
        pref = context.getSharedPreferences("AndroidHivePref", 0);
        editor = pref.edit();

        list_expressions.clear();
        list_expressions = new ArrayList<>(pref.getStringSet(KEY_REGEX, null));
    }

    private void searchCode(String bodySMS, Context context) {
        for (String REGULAR_EXPRESSION_SELECTED : list_expressions) {
            Pattern p = Pattern.compile(REGULAR_EXPRESSION_SELECTED, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(bodySMS);
            Log.d(TAG, bodySMS);
            if (m.matches()) {
                Log.d(TAG + "2", m.matches() + " " + bodySMS + " " + p.toString());

                // Get code from SMSBody
                String code = bodySMS.split(" ", 2)[0];

                // Send code to server
                new SendCode(code, simReader.getPhoneNumber(),
                        simReader.getSubscriberId(), false, context);
                break;
            }
        }
    }
}
