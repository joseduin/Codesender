package codesender.codesender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

public class AddRegularExpressionModal extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "AddRegularExpressionMod";

    private EditText txtRegexExpresion;
    private ImageButton addNewRegularExpression;
    private ViewGroup container;
    private Button closeModal, goWIKI;

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // All Shared Preferences Keys
    public static final String KEY_REGEX = "regex";

    private ArrayList<String> list_expressions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_regular_expression_modal);

        txtRegexExpresion = (EditText) findViewById(R.id.txtRegexExpresion);
        addNewRegularExpression = (ImageButton) findViewById(R.id.addNewRegularExpression);
        container = (ViewGroup) findViewById(R.id.container);
        closeModal = (Button) findViewById(R.id.closeModal);
        goWIKI = (Button) findViewById(R.id.goWIKI);

        printRegularExpressionsOnScreem();
        addNewRegularExpression.setOnClickListener(this);
        closeModal.setOnClickListener(this);
        goWIKI.setOnClickListener(this);
    }

    private void printRegularExpressionsOnScreem() {
        pref = this.getApplicationContext().getSharedPreferences("AndroidHivePref", 0);
        editor = pref.edit();

        list_expressions = new ArrayList<>(pref.getStringSet(KEY_REGEX, null));

        for (int i = 0; i < list_expressions.size(); i++) {

            String exp = list_expressions.get(i);
            createExpressionItem(exp, i);
        }
    }

    private void createExpressionItem(String exp, final int index) {
        final ViewGroup row = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_add_regular_expression_modal_item, container, false);

        TextView expression = (TextView) row.findViewById(R.id.expression);
        ImageButton removeExpression = (ImageButton) row.findViewById(R.id.removeExpression);

        expression.setText(exp);
        removeExpression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeExpresion(row);
            }
        });

        // Add to container
        container.addView(row);
    }

    private void removeExpresion(ViewGroup row) {
        container.removeView(row);
        int index = 0;
        for (int i = 0; i < list_expressions.size(); i++) {
            String exp = list_expressions.get(i);
            TextView expression = (TextView) row.getChildAt(0);
            if (exp.equals(expression.getText().toString())) {
                index = i;
            }
        }
        list_expressions.remove(index);

        // Update cache of list expressions
        editor.putStringSet(KEY_REGEX, new HashSet<>(list_expressions));
        editor.commit();
    }

    private void addExpression() {
        String newExpression = txtRegexExpresion.getText().toString().trim();
        if (!newExpression.isEmpty()) {

            txtRegexExpresion.setText("");

            createExpressionItem(newExpression, list_expressions.size());
            list_expressions.add(newExpression);

            // Update cache of list expressions
            editor.putStringSet(KEY_REGEX, new HashSet<>(list_expressions));
            editor.commit();
        } else {
            Toast.makeText(this, "Empty field.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeModal:
                onBackPressed();
                break;
            case R.id.addNewRegularExpression:
                addExpression();
                break;
            case R.id.goWIKI:
                Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://en.wikipedia.org/wiki/Regular_expression"));
                startActivity(viewIntent);
                break;
        }

    }
}
