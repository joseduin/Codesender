package codesender.codesender;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jose on 21/12/2016.
 */

public class SplashScreen extends RuntimePermissionsActivity implements View.OnClickListener {

    private static final String TAG = "SplashScreen";
    private static final int REQUEST_PERMISSIONS = 20;
    private FloatingActionButton fab;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton)findViewById(R.id.fab_continue);
        textView = (TextView)findViewById(R.id.textHide);

        fab.setOnClickListener(this);
        hideTextView();

//        textView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                textView.setVisibility(View.INVISIBLE);
//            }
//        },2500);


    }

    private void permissions() {
        SplashScreen.super.requestAppPermissions(new
                        String[]{Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_SMS},
                R.string.runtime_permissions_txt, REQUEST_PERMISSIONS);
    }

    private void hideTextView() {
        int delayMillis = 3000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                TranslateAnimation animation =
                        new TranslateAnimation(0,-displayMetrics.widthPixels,0,0);
                animation.setDuration(1000);
                animation.setFillAfter(true);
                textView.startAnimation(animation);
                textView.setVisibility(View.GONE);
            }
        },delayMillis);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if(requestCode == REQUEST_PERMISSIONS) {
            askForContactPermission(requestCode);
            startActivity(new Intent(this, SearchCode.class));//MainActivity.class));
        }
    }

    public void askForContactPermission(int code){
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        //    Toast.makeText(this, "Permissions Received.", Toast.LENGTH_LONG).show();
        //}
        Log.d(TAG + "CODE------", "" + code);
    }

    @Override
    public void onClick(View v) {
        permissions();
    }
}
