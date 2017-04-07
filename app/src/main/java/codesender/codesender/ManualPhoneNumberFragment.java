package codesender.codesender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Jose on 21/12/2016.
 */

public class ManualPhoneNumberFragment extends Fragment {//AppCompatActivity {

    private static final String TAG = "ManualPhoneNumberFragme";
    private EditText editText;
    private Button saveButton;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_phone_dialog, container, false);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_general, false);
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = pref.edit();

        editText = (EditText) v.findViewById(R.id.phoneToSave);
        saveButton = (Button) v.findViewById(R.id.savePhone);
        saveButton.setEnabled(false);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    saveButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString(getResources().getString(R.string.KEY_PHONE_NUMBER), editText.getText().toString());
                editor.commit();

                Log.d(TAG, editText.getText().toString());

                SimReader simReader = new SimReader(getActivity());
                simReader.setPhoneNumber(editText.getText().toString());
                simReader.sendPhoneNumberToServer();


                /*Intent intent = new Intent(getActivity(), SearchCode.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
            }
        });

        getPhoneNumberInPreferences();

        return v;
    }

    /** If exist a phone number, show in the PHONE NUMBER FIELD
    * */
    private void getPhoneNumberInPreferences() {
        String phone_number = pref.getString(getResources().getString(R.string.KEY_PHONE_NUMBER), null);
        if (phone_number != null) {
            editText.setText(phone_number);
        }
    }
/*
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_phone_dialog);

    }
*/
}
