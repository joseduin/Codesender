package codesender.codesender;

import android.content.Context;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by Jose on 21/12/2016.
 */

public class SendCode {

    private String code;
    private String phoneNumber;
    private String imsiCode;
    private Context context;
    private boolean pogress;

    public SendCode(String code, String phoneNumber, String imsiCode, Context context) {
        this.code = code;
        this.phoneNumber = phoneNumber;
        this.imsiCode = imsiCode;
        this.context = context;
        this.pogress = true;
        new SendRegistrationForm(this.context).execute();
    }

    public SendCode(String code, String phoneNumber, String imsiCode, boolean pogress, Context context) {
        this.code = code;
        this.phoneNumber = phoneNumber;
        this.imsiCode = imsiCode;
        this.context = context;
        this.pogress = pogress;
        new SendRegistrationForm(this.context).execute();
    }

    public boolean isPogress() { return pogress; }

    public void setPogress(boolean pogress) { this.pogress = pogress; }

    public String getCode() {
            return code;
            }

    public void setCode(String code) {
            this.code = code;
            }

    public String getPhoneNumber() {
            return phoneNumber;
            }

    public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            }

    public String getImsiCode() {
            return imsiCode;
            }

    public void setImsiCode(String imsiCode) {
            this.imsiCode = imsiCode;
            }

    private class SendRegistrationForm extends AsyncTask<Void,Void,String> {

        private static final String TAG = "SendRegistrationForm";
        private ProgressDialog progressDialog;

        public SendRegistrationForm(Context activity) {
            if (pogress) {
                this.progressDialog = new ProgressDialog(activity);
            }
        }

        @Override
        protected void onPreExecute() {
            if (pogress) {
                progressDialog.setMessage("Loading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void...params){

            try {

                if(phoneNumber.equals("") || phoneNumber == null)
                    phoneNumber = PreferenceManager.getDefaultSharedPreferences(context).
                            getString("phone_number", "undefined");

                String message = "[imsi_code->" + imsiCode + " , phone_number->" + phoneNumber + " , code->" + code + "]\n";

                SendPostRequest send = new SendPostRequest(code, imsiCode, phoneNumber, false, context);
                        //message, "code", context);

                String response = send.senData();
                Log.d(TAG, response);
                return response;

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "failed";
        }

        @Override
        protected void onPostExecute(String s) {
            if (pogress) {
                if(s.equals("RESULT_OK")){
                    Log.d(TAG, "Code sent successfully !");
                    Toast.makeText(context, "Code sent successfully !", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d(TAG, "Try Again !");
                    Toast.makeText(context, "Try Again !", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "RESPONSE " + s);
                progressDialog.dismiss();
            }
        }
    }
}
