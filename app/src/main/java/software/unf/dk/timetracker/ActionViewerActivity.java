package software.unf.dk.timetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Date;

public class ActionViewerActivity extends AppCompatActivity {
    public static final String ACTION_NAME_EXTRA = "ACTION_NAME";
    public static final String ACTION_CLASS_NAME_EXTRA = "ACTION_CLASS_NAME";
    public static final String ACTION_DATE_EXTRA = "ACTION_DATE";
    public static final int ACTION_VIEW_REQUEST = 1;

    private TextView actionText;
    private Spinner classificationSpinner;

    private String[] spinnerStrings;
    private String classificationString;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_view);

        layoutSetup();
    }

    void layoutSetup() {
        actionText = (TextView) findViewById(R.id.actionText);
        classificationSpinner = (Spinner) findViewById(R.id.classificationSpinner);
        TextView timeText = (TextView) findViewById(R.id.timeText);

        Intent intent = getIntent();
        String name = intent.getStringExtra(ACTION_NAME_EXTRA);
        Date date = new Date(intent.getLongExtra(ACTION_DATE_EXTRA, new Date().getTime()));
        String classificationName = intent.getStringExtra(ACTION_CLASS_NAME_EXTRA);

        actionText.setText(name);
        timeText.setText(date.toString());
        setSpinner();

        int classIndex = 0;
        for (int i = 0; i < spinnerStrings.length; i++) {
            if (spinnerStrings[i].equals(classificationName)) classIndex = i;
        }
        classificationSpinner.setSelection(classIndex);
    }

    void setSpinner() {
        spinnerStrings = Classification.mapToStringList(Classification.classificationMap).toArray(new String[0]);

        // Create adapter to translate string array into spinner contents
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerStrings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classificationSpinner.setAdapter(adapter);
        // Listen to things happens on the Spinner
        classificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Set string to selected spinner value
                classificationString = spinnerStrings[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    /*void accept() {
        Intent intent = new Intent();
        intent.putExtra(ACTION_NAME_EXTRA, actionText.getText().toString());
        intent.putExtra(ACTION_CLASS_NAME_EXTRA, classificationString);
        setResult(ACTION_VIEW_REQUEST, intent);
        finish();
    }

    void cancel() {
        finish();
    }*/
}
