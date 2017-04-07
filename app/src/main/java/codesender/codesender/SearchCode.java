package codesender.codesender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.CursorLoader;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCode extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

	// sms service to post
	// link to wiiki regular expressions in the modal view

    private static final String TAG = "SearchCode";

    private Spinner regexExpresion;
    private Button searchAndSend;
    private ImageButton addRegularRegex;
    private TextView phone_numbe;

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // All Shared Preferences Keys
    public static final String KEY_REGEX = "regex";

    private ArrayList<String> defaultRegexp = new ArrayList<>();
    private String REGULAR_EXPRESSION_SELECTED;
    private String response = "Empty";
    private SimReader simReader;
    private boolean matches = false;
    private boolean codeOld = false;

    private Boolean firstTime = null;
    private SharedPreferences prefDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_code);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.search_edit_text);

        prefDefault = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pref = this.getApplicationContext().getSharedPreferences("AndroidHivePref", 0);
        editor = pref.edit();

        new GetCodesPosted().execute();
        simReader = new SimReader(SearchCode.this);

        regexExpresion = (Spinner) findViewById(R.id.regexExpresion);
        searchAndSend = (Button) findViewById(R.id.searchAndSend);
        addRegularRegex = (ImageButton) findViewById(R.id.addRegularRegex);
        phone_numbe = (TextView) findViewById(R.id.phone_numbe);

        setRegexExpresion();

        //PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.pref_general, false);
        //prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Log.d("SIM", simReader.getPhoneNumber());
        appBody();
        Log.d("SIM", simReader.getPhoneNumber());

        searchAndSend.setOnClickListener(this);
        addRegularRegex.setOnClickListener(this);
        regexExpresion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                REGULAR_EXPRESSION_SELECTED = defaultRegexp.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Clear spinner
        defaultRegexp.clear();
        regexExpresion.setAdapter(null);

        // Refresh list of regular expression
        defaultRegexp = new ArrayList<>(pref.getStringSet(KEY_REGEX, null));
        Log.d(TAG, defaultRegexp.isEmpty() + " " + defaultRegexp.size());
        if (defaultRegexp.isEmpty()) {
            setRegexExpresion();
        } else {
            loadSpinner();
        }
    }

    private void setRegexExpresion() {
        defaultRegexp.clear();

        // Debug lines
        //editor.clear();
        //editor.commit();

        // Default regular expressions

        if (pref.getStringSet(KEY_REGEX, null) == null) {
            defaultRegexp = new ArrayList<>();
        } else {
            defaultRegexp = new ArrayList<>(pref.getStringSet(KEY_REGEX, null));
        }

        if (defaultRegexp.isEmpty()) {
            defaultRegexp.add("(.*)[C-c]ode(.*)");
            defaultRegexp.add("(.*)is your (.*) verification(.*)");
            defaultRegexp.add("(.*)confirmation code");

            REGULAR_EXPRESSION_SELECTED = defaultRegexp.get(0);

            editor.putStringSet(KEY_REGEX, new HashSet<>(defaultRegexp));
            editor.commit();
        } else {

            // Set exist regular expressions
            for (String regular : pref.getStringSet(KEY_REGEX, null)) {
                defaultRegexp.add(regular);
            }
        }

        loadSpinner();
    }

    private void loadSpinner() {
        // Set all regular expression to spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultRegexp);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regexExpresion.setAdapter(dataAdapter);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, response);
        Uri uri = Uri.parse("content://sms/inbox"); // "content://sms/inbox"
        String[] projection = new String[]{"_id", "thread_id", "address", "person", "body", "date", "type"};
        return new CursorLoader(this.getBaseContext(), uri, projection, null, null, "date desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        matches = false;
        codeOld = false;

        if(c.moveToFirst()) {
            do {
                String bodySMS = c.getString(c.getColumnIndexOrThrow("body")).toString();

                Pattern p = Pattern.compile(REGULAR_EXPRESSION_SELECTED, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(bodySMS);
                Log.d(TAG, bodySMS);
                if (m.matches()) {
                    matches = true;
                    Log.d(TAG + "2", m.matches() + " " + bodySMS + " " + p.toString());

                    // Get code from SMSBody
                    String code = bodySMS.split(" ", 2)[0];
                    boolean b = validatePostedCodes(code);

                    // Send code to server
                    if (!b) {
                        codeOld = true;

                        // Error overflow get request
                        new SendCode(code, simReader.getPhoneNumber(),
                                    simReader.getSubscriberId(), SearchCode.this);

                        // Refresh
                        response += "Phone number: " + simReader.getPhoneNumber() + " Imsi code: " + simReader.getSubscriberId() + " Code: " + code;
                    }
                }
            } while (c.moveToNext());
        }

        if (!matches) {
            Toast.makeText(this, "No matches foundn.", Toast.LENGTH_SHORT).show();
        } else {
            if (!codeOld) {
                Toast.makeText(this, "You don't have a code to post.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validatePostedCodes(String code) {
        boolean b = false;
        Pattern p = Pattern.compile("(.*)Code: " + code + "(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(response);
        Log.d(TAG + "_resp",p.toString() + " " + m.matches() + " " + response);
        if (m.matches()) {
            b = true;
        }
        return b;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        matches = false;
        codeOld = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchAndSend:
                if (!response.equals("Empty")) {
                    if (prefDefault.getString(getResources().getString(R.string.KEY_PHONE_NUMBER), null) != null) {
                        getSupportLoaderManager().initLoader(1, null, this);
                    } else {
                        Toast.makeText(getApplication(), "First enter your phone number", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Wait a second to server connection.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.addRegularRegex:
                Intent i = new Intent(SearchCode.this, AddRegularExpressionModal.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            startActivity(new Intent(SearchCode.this, SettingsActivity.class));

        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            /*case R.id.action_send:
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
                return true;
            */default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
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

    public void showDialog(){
        Intent intent = new Intent(getApplicationContext(), ManualPhoneNumberFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void appBody(){
        Log.d("SIM", simReader.getPhoneNumber());
        simReader = new SimReader(SearchCode.this);
        Log.d("SIM", simReader.getPhoneNumber());
        if(isFirstTime()) {
            SimReader.storePhoneNumberOnPreferences(simReader, pref);
            Log.d("SIM", simReader.getPhoneNumber());
        }

        Log.d("SIM", simReader.getPhoneNumber());
        if (!simReader.getPhoneNumber().isEmpty()) {
            phone_numbe.setText(getResources().getString(R.string.phone_number) + " " + simReader.getPhoneNumber());
        }
        //codeInput = (EditText) findViewById(R.id.codeInput);

        //sendCodeButton = (Button) findViewById(R.id.button);

        //assert sendCodeButton != null;

        //sendCodeButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        enviarCodigo();
        //    }
        //});

        Intent myIntent = new Intent(SearchCode.this, CheckSIMState.class);
        // PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
        this.sendBroadcast(myIntent);

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }

    private class GetCodesPosted extends AsyncTask<Void,Void,String> {

        private static final String TAG = "GetCodesPosted";

        public GetCodesPosted() {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void...params){
            while (response.equals("Empty")) {
                try {
                    SendPostRequest send = new SendPostRequest("empty", "empty",
                            "empty", true, SearchCode.this);

                    response = send.senData();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }

}
