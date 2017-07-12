package software.unf.dk.timetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Determines what kind of time range should be displayed
 */
enum WhatToShow {ALL, DAY, WEEK, MONTH, YEAR}

/**
 * A list-based view of the activities the user has logged
 */
public class HistoryActivity extends AppCompatActivity {

    private String[] classificationSpinnerStrings;
    // Currently selected string in spinner
    private String classificationString;

    // List.
    private ListView actionListView;

    // Text view.
    private TextView showDate;

    /**
     * Currently shown date in string form
     */
    private String currShownDate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM yyyy", Locale.ENGLISH);



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_view);
        layoutSetup();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActionViewerActivity.ACTION_VIEW_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra(ActionViewerActivity.ACTION_NAME_EXTRA);
            Classification classification = Classification.getClassificationByName(data.getStringExtra(ActionViewerActivity.ACTION_CLASS_NAME_EXTRA));

        }
    }*/

    private void layoutSetup() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        // Enable back button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.w("timetracker", "Warning: Failed to get action bar!");
        }

        setReferences();
        currShownDate = dateFormat.format(new Date());

        updateView();
    }

    private void updateView(){
        final Context context = this;
        showDate.setText(currShownDate);
        Action[] values = setValues(WhatToShow.DAY, currShownDate);

        ArrayAdapter<Action> adapter = new ActionArrayAdapter(this, values);
        actionListView.setAdapter(adapter);

        actionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Action action = (Action) actionListView.getItemAtPosition(position);

                // Build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit entry");
                View viewInflated = LayoutInflater.from(context).inflate(R.layout.activity_action_view, parent, false);
                final TextView actionText = viewInflated.findViewById(R.id.actionText);
                final TextView descriptionText = viewInflated.findViewById(R.id.descriptionText);
                final TextView timeText = viewInflated.findViewById(R.id.timeText);
                Spinner classificationSpinner = viewInflated.findViewById(R.id.classificationSpinner);
                actionText.setText(action.getName());
                descriptionText.setText(action.getDescription());
                timeText.setText(action.getDate().toString());

                // Set spinner
                classificationSpinnerStrings = Classification.mapToStringList(Classification.classificationMap).toArray(new String[0]);
                // Create adapter to translate string array into spinner contents
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, classificationSpinnerStrings);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                classificationSpinner.setAdapter(adapter);
                // Listen to things happens on the Spinner
                classificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        // Set string to selected spinner value
                        classificationString = classificationSpinnerStrings[i];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });
                // Done setting spinner
                builder.setView(viewInflated);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        action.setName(actionText.getText().toString());
                        action.setClassification(Classification.getClassificationByName(classificationString));
                        action.setDescription(descriptionText.getText().toString());
                        updateView();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Action.actionList.remove(action);
                        updateView();
                        dialog.dismiss();
                    }
                });

                builder.show();

                // TODO: discuss if Actions should be edited in a separate Activity, and how to implement that
            }
        });
    }

    private void setReferences(){
        // List.
        actionListView = (ListView) findViewById(R.id.actionList);

        // Text view.
        showDate = (TextView) findViewById(R.id.showDate);
    }

    private Action[] setValues(WhatToShow whatToShow, String date){
        ArrayList<Action> values = new ArrayList<>();

        // Find out what values to find.
        switch (whatToShow){
            case ALL:
                values = Action.actionList;
                break;
            case DAY:
                values = getDayData(date);
                break;
            case WEEK:
                //values = getWeekData(date);
                values = new ArrayList<>();
                break;
            case MONTH:
                values = getMonthData(date);
                break;
            case YEAR:
                values = getYearData(date);
                break;
        }
        for (Action a :
                values) {
            Log.e("Test", "address is " + a);
        }

        // Make the list a array.
        Action[] result = values.toArray(new Action[0]);
        //  Reverse the array.
        for(int i = 0; i < result.length / 2; i++) {
            Action temp = result[i];
            result[i] = result[result.length - i - 1];
            result[result.length - i - 1] = temp;
        }
        return result;
    }

    private ArrayList<Action> getDayData(String date){
        ArrayList<Action> result = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM yyyy", Locale.ENGLISH);

        for (Action a : Action.actionList) {
            // Get the date of the acton.
            String aDate = dateFormat.format(a.getDate());
            // Is it the day we are looking after? Yes, then add it.
            if (aDate.equals(date)) {
                result.add(a);
            }
        }
        return result;
    }

    /*private ArrayList<Action> getWeekData(String date){
        ArrayList<Action> result = new ArrayList<>();


        // Not Yet working.


        //return result;
        return new ArrayList<>();
    }*/

    private ArrayList<Action> getMonthData(String month){
        ArrayList<Action> result = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("MM yyyy", Locale.ENGLISH);

        for (Action a : Action.actionList) {
            // Get the date of the acton.
            String aDate = dateFormat.format(a.getDate());
            // Is it the day we are looking after? Yes, then add it.
            if(aDate.equals(month)){
                result.add(a);
            }
        }
        return result;
    }


    private ArrayList<Action> getYearData(String year){
        ArrayList<Action> result = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);

        for (Action a : Action.actionList) {
            // Get the date of the acton.
            String aDate = dateFormat.format(a.getDate());
            // Is it the day we are looking after? Yes, then add it.
            if(aDate.equals(year)){
                result.add(a);
            }
        }
        return result;
    }

    /*
     * For Button Input.
     */
    public void oneExtraDay(View view){
        // Add a day to the current date displayed.
        Calendar c = Calendar.getInstance();
        try{
            c.setTime(dateFormat.parse(currShownDate));
        }
        catch (ParseException e){
            return;
        }

        c.add(Calendar.DATE, 1);  // number of days to add
        currShownDate = dateFormat.format(c.getTime());  // currShownDate is now the new date
        // Update display.
        updateView();
    }
    public void oneLessDay(View view){
        // Add a day to the current date displayed.
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(currShownDate));
        } catch (ParseException e) {
            return;
        }

        c.add(Calendar.DATE, -1);  // number of days to add
        currShownDate = dateFormat.format(c.getTime());  // currShownDate is now the new date
        // Update display.
        updateView();
    }

    public void oneExtraMonth(View view){
        // Add a month to the current date displayed.
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(currShownDate));
        } catch (ParseException e){
            return;
        }

        c.add(Calendar.DATE, 30);  // number of days to add
        currShownDate = dateFormat.format(c.getTime());  // dt is now the new date
        // Update display.
        updateView();
    }
    public void oneLessMonth(View view){
        // Add a day to the current date displayed.
        Calendar c = Calendar.getInstance();
        try{
            c.setTime(dateFormat.parse(currShownDate));
        }
        catch (ParseException e){
            return;
        }

        c.add(Calendar.DATE, -30);  // number of days to add
        currShownDate = dateFormat.format(c.getTime());  // currShownDate is now the new date
        // Update display.
        updateView();
    }

    public void setDateToday(View view){
        currShownDate = dateFormat.format(new Date());
        updateView();
    }

}

class ActionArrayAdapter extends ArrayAdapter<Action> {
    private final Context context;
    private final Action[] values;

    ActionArrayAdapter(Context context, Action[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Action a = values[position];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_history, parent, false);
        TextView nameText = convertView.findViewById(R.id.nameText);
        TextView timeText = convertView.findViewById(R.id.timeText);
        TextView dateText = convertView.findViewById(R.id.dateText);
        TextView classificationText = convertView.findViewById(R.id.classificationText);

        DateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM yyyy", Locale.ENGLISH);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        nameText.setText(a.getName());
        classificationText.setText(a.getClassification().getName());
        timeText.setText(timeFormat.format(a.getDate()));
        dateText.setText(dateFormat.format(a.getDate()));

        return convertView;
    }



}