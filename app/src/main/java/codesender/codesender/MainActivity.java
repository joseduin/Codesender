package codesender.codesender;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Fragment { //AppCompatActivity {implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private EditText codeInput;
    private Button sendCodeButton;
    private SimReader simReader;
    private Boolean firstTime = null;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final int REQUEST_PERMISSIONS = 20;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_main, container, false);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_general, false);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = pref.edit();

        appBody(v);

        return v;
    }
/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        } //else if (id == R.id.action_search) {
          //  startActivity(new Intent(this, SearchCode.class));
          //  return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = getActivity().getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.putString("simcardserial", simReader.getSimSerialNumber());
                Log.d(TAG, "Storing SIM Serial " + simReader.getSimSerialNumber());
                editor.commit();
                if(simReader.getPhoneNumber().equals("") || simReader.getPhoneNumber() == null){
                    showDialog();

                } else{
                    simReader.sendPhoneNumberToServer();
                }

                Log.d(TAG, "SIM HAS BEEN SAVED");

            } else {

                if(SimReader.isSIMChanged(simReader, mPreferences)){
                    mPreferences.edit().putString("simcardserial",simReader.getSimSerialNumber()).apply();
                    if(simReader.getPhoneNumber().equals("") || simReader.getPhoneNumber() == null){
                        showDialog();

                    } else {
                        simReader.sendPhoneNumberToServer();
                    }

//                    showDialog();
                    Log.d(TAG, "SIM  " + "HAS BEEN CHANGED");
                }
                else
                    Log.d(TAG, "SIM  " + "ITs THE SAME SIM");
            }
        }
        return firstTime;
    }
/*
    public void showDialog() {
        Intent intent = new Intent(getApplicationContext(), ManualPhoneNumberFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
*/
    private void enviarCodigo() {
        if (pref.getString(getResources().getString(R.string.KEY_PHONE_NUMBER), null) != null) {
            if (!codeInput.getText().toString().trim().equals("")) {
                new SendCode(codeInput.getText().toString(), simReader.getPhoneNumber(),
                        simReader.getSubscriberId(), getActivity());
            } else {
                Toast.makeText(getActivity(), "Empty code.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "First enter your phone number", Toast.LENGTH_LONG).show();
        }
    }

    public void appBody(View v){
       /* simReader = new SimReader(MainActivity.this);
        if(isFirstTime()) {
            SimReader.storePhoneNumberOnPreferences(simReader, prefs);
        }
*/
        codeInput = (EditText) v.findViewById(R.id.codeInput);
        sendCodeButton = (Button) v.findViewById(R.id.button);

        assert sendCodeButton != null;

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCodigo();
            }
        });

        Intent myIntent = new Intent(getActivity(), CheckSIMState.class);
        // PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
        getActivity().sendBroadcast(myIntent);

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }
}
