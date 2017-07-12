package software.unf.dk.timetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import static software.unf.dk.timetracker.MainActivity.setNotificationTime;


public class SettingsActivity extends AppCompatActivity {
    private Spinner spinner;
    private EditText classificationEntry;
    private String classificationName;
    private String newName;
    private ToggleButton toggle;
    private EditText notificationTimeText;

    private String[] spinnerStrings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        layoutSetup();
        setToggle();
    }

    private void layoutSetup() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.w("timetracker", "Warning: Failed to get action bar!");
        }

        // Dropdown.
        spinner = (Spinner) findViewById(R.id.spinner);
        classificationEntry = (EditText) findViewById(R.id.classificationText);
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        notificationTimeText = (EditText) findViewById(R.id.notificationTimeText);

        setSpinner();
    }

    private void setSpinner(){
        spinnerStrings = Classification.mapToStringList(Classification.classificationMap).toArray(new String[0]);

        // Doing so the Array can be put into the Spinner
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerStrings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // Listen to things happens on the Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                classificationName = spinnerStrings[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void adding(View view){
        String name = classificationEntry.getText().toString();

        if (!Classification.createNew(name)) {
            Toast.makeText(this, "Category already exists!", Toast.LENGTH_LONG).show();
            setSpinner();
            return;
        }

        setSpinner();
        classificationEntry.setText("");
    }

    public void remove(View view){
        Classification c = Classification.getClassificationByName(classificationName);
        if (c != null) c.setVisible(false);
        setSpinner();
    }

    public void rename(View view){
        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new category name");

        // Set text input
        final EditText inputText = new EditText(this);
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setText(classificationName);
        inputText.setSelectAllOnFocus(true);
        builder.setView(inputText);

        // Define OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newName = inputText.getText().toString();
                if (newName.equals("")) {
                    showToast("Name can't be empty");
                }
                if (Classification.getClassificationByName(newName) != null) {
                    showToast("Name must be unique");
                    return;
                }
                dialog.dismiss();
                Classification c = Classification.getClassificationByName(classificationName);
                if (c != null) c.setName(newName);
                setSpinner();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public void setToggle() {
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.setWantNotification(true);
                } else {
                    MainActivity.setWantNotification(false);
                }
            }
        });
    }


    public void updateNotificationTime(View view) {
        int time;
        try {
            time = Integer.parseInt(notificationTimeText.getText().toString());
            notificationTimeText.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_LONG);
            return;
        }
        setNotificationTime(time);
    }


}
