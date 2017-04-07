package codesender.codesender;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by Jose on 21/12/2016.
 */

public class SimReader {

    private static final String TAG = "SimReader";
    private TelephonyManager telephonyManager;
    private String simSerialNumber;
    private String phoneNumber;
    private String subscriberId;
    private String deviceId;
    private String networkOperatorName;
    private String countryISO;
    Context context;

    public SimReader(Context c) {
        this.context = c;
        this.telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        this.phoneNumber = this.telephonyManager.getLine1Number();
        this.simSerialNumber = this.telephonyManager.getSimSerialNumber();
        this.subscriberId = this.telephonyManager.getSubscriberId();
        this.deviceId = this.telephonyManager.getDeviceId();
        this.networkOperatorName = this.telephonyManager.getNetworkOperatorName();
        this.countryISO = this.telephonyManager.getNetworkCountryIso();

        Log.d(TAG, "telephonyManager->" + telephonyManager + ", phoneNumber-> " + phoneNumber + ", simSerialNumber-> " + simSerialNumber + ", subscriberId->" + subscriberId +
                ", deviceId->" + deviceId + ", networkOperatorName->" + networkOperatorName + ", countryISO ->" + countryISO);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSimSerialNumber() {
        return simSerialNumber;
    }

    public void setSimSerialNumber(String simSerialNumber) {
        this.simSerialNumber = simSerialNumber;
    }

    public TelephonyManager getTelephonyManager() {
        return telephonyManager;
    }

    public void setTelephonyManager(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        this.networkOperatorName = networkOperatorName;
    }

    public String getCountryISO() {
        return countryISO;
    }

    public void setCountryISO(String countryISO) {
        this.countryISO = countryISO;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void sendPhoneNumberToServer(){

        if(this.phoneNumber.equals("") || this.phoneNumber == null)
            this.phoneNumber = "undefined";

        new SendRegistrationForm(context).execute();
    }
    public void sendPhoneNumberToServerBg(SharedPreferences preferences){

        if(this.phoneNumber.equals("") || this.phoneNumber == null)
            this.phoneNumber = "undefined";

        new SendRegistrationFormBg(context, preferences).execute();
    }

    public static boolean isSIMChanged(SimReader simReader, SharedPreferences preferences) {
        String simSerial =  preferences.getString("simcardserial", "xxx");
        if(!simSerial.equals(simReader.getSimSerialNumber()))
            return true;
        else
            return false;
    }

    public static boolean storePhoneNumberOnPreferences(SimReader simReader, SharedPreferences preferences) {
        Log.d(TAG, simReader.getPhoneNumber());
        return preferences.edit().putString("KEY_PHONE_NUMBER", simReader.getPhoneNumber()).commit();
    }

    public boolean emptyPhoneNumber(){
        return ((this.phoneNumber == null) || (this.phoneNumber.equals("")));
    }


    private class SendRegistrationForm extends AsyncTask<Void,Void,String> {

        private static final String TAG = "SendRegistrationForm";
        //ProgressDialog progressDialog;

        public SendRegistrationForm(Context activity) {
        //    this.progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
        //    progressDialog.setMessage("Loading...");
        //    progressDialog.setIndeterminate(true);
        //    progressDialog.setCancelable(false);
        //    progressDialog.show();
        }

        @Override
        protected String doInBackground(Void...params){
            String response = "";
            while (!response.equals("RESULT_OK")){
                try {
                    SendPostRequest send = new SendPostRequest("empty", subscriberId, phoneNumber, false, context);

                    response = send.senData();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("RESULT_OK")){
                Toast.makeText(context, "Welcome to CodeSender !", Toast.LENGTH_LONG).show();
            }
//            else{
//
//                        Toast.makeText(context, "Por favor intenta de nuevo", Toast.LENGTH_LONG).show();
//            }
            Log.d(TAG, "RESPONSE" + s);
        //    progressDialog.dismiss();
        }
    }

    private class SendRegistrationFormBg extends AsyncTask<Void,Void,String> {

        private static final String TAG = "SendRegistrationFormBg";
        ProgressDialog progressDialog;
        SharedPreferences sharedPreferences;

        public SendRegistrationFormBg(Context activity, SharedPreferences preferences) {
            //this.progressDialog = new ProgressDialog(activity);
            this.sharedPreferences = preferences;
        }

        @Override
        protected void onPreExecute(){
//            progressDialog.setMessage("Loading...");
//            progressDialog.setIndeterminate(true);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void...params){
            String response = "";
            while (!response.equals("RESULT_OK")){
                try {
                    SendPostRequest send = new SendPostRequest("empty", subscriberId, phoneNumber, false, context);
                            //message,"phoneNumber", context);
                    response = send.senData();
                    //return response;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("RESULT_OK")){
//                Toast.makeText(context, "Welcome to CodeSender !", Toast.LENGTH_LONG).show();
                sharedPreferences.edit().putString("KEY_PHONE_NUMBER", phoneNumber).apply();
            }
//            else{
//
//                        Toast.makeText(context, "Por favor intenta de nuevo", Toast.LENGTH_LONG).show();
//            }
            Log.d(TAG, "RESPONSE" + s);
//            progressDialog.dismiss();
        }
    }
}
