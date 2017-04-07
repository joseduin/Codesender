package codesender.codesender;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jose on 21/12/2016.
 */

public class SendPostRequest {

    private static final String TAG = "SendPostRequest";
    private URL url;
    private Context context;
    HttpURLConnection conn;
    private String urlData;
    private String response = "";

    public SendPostRequest(String code, String imsi_code, String phone_number, boolean getData, Context context) throws UnsupportedEncodingException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = preferences.getString("server_address", "http://codesender.tk/");
        //preferences.getString("server_address", "https://php-joseduin.c9users.io/");
        // "http://sombonetwork.com/");
        //String content = URLEncoder.encode(content_, "UTF-8");
        //String type = URLEncoder.encode(type_, "UTF-8");
        try {
            this.url = new URL("https://php-joseduin.c9users.io/codesender.php");//"http://codesender.tk/codesender.php");
                    //"dev2.mp3ify.com/codesender");
                    //URL("https://php-joseduin.c9users.io/codesender.php");
            Log.d(TAG, this.url.toString() + "code=" + code + "&imsi_code=" + imsi_code + "&phone_number=" + phone_number);

            this.urlData = "code=" + code + "&imsi_code=" + imsi_code + "&phone_number=" + phone_number + "&getData=" + getData;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String senData() {
        try {
            this.conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(this.urlData);
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public SendPostRequest() throws UnsupportedEncodingException {
        try {
            this.url = new URL("http://codesender.tk/code.txt");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getData() {
        try {
            this.conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
