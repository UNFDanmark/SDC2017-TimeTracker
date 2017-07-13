package software.unf.dk.timetracker;

import android.content.Context;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private ListView statListView;

    // Spinner
    private Spinner spinner;
    private static String[] spinnerStrings;
    private String classificationString;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        layoutSetup();
    }

    protected void onResume(){
        super.onResume();
        setSpinner();
    }

    private void layoutSetup() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.statistics_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.w("timetracker", "Warning: Failed to get action bar!");
        }

        setReferences();
        updateView();
    }

    private void setReferences(){
        // List.
        statListView = (ListView) findViewById(R.id.underCategoryList);
        spinner = (Spinner)findViewById(R.id.categoryChooser);
    }

    private void updateView() {
        ArrayList<String> valuesToRead = new ArrayList<>();
        valuesToRead.add("category");

        // Find the values to read.
        for (Action a : Action.actionList) {
            if (a.getClassification().getName().equals(classificationString)) {
                // The one we are looking for.
                // Do we already have it?
                if (valuesToRead.contains(a.getName()))
                    continue;

                valuesToRead.add(a.getName());
            }
        }

        // Convert the list back to an array.
        String[] valuesToSend = valuesToRead.toArray(new String[0]);
        ArrayAdapter<String> adapter = new StatisticsArrayAdapter(this, valuesToSend, classificationString);
        statListView.setAdapter(adapter);
    }

    private void setSpinner(){
        spinnerStrings = Classification.mapToStringList(Classification.classificationMap).toArray(new String[0]);
        classificationString = spinnerStrings[0];

        // Doing so the Array can be put into the Spinner
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(StatisticsActivity.this,
                android.R.layout.simple_spinner_item, spinnerStrings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // Listen to things happens on the Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                              @Override
                                              public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                  // Set string to selected spinner value
                                                  classificationString = spinnerStrings[i];
                                                  Log.i("Test", "Hi the class text have been set with: " + classificationString);
                                                  updateView();
                                              }

                                              @Override
                                              public void onNothingSelected(AdapterView<?> adapterView) { }
                                          }
        );
    }

}

class StatisticsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final String category;

    StatisticsArrayAdapter(Context context, String[] values, String category) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.category = category;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String name = values[position];

        if(name.equals("category")){
            // If it is the first line, make a pieChart.

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_statistics_pie, parent, false);
            TextView nameText = convertView.findViewById(R.id.nameOfPieChart);
            PieChart chart = convertView.findViewById(R.id.chart);

            nameText.setText(category);

            ArrayList<String> labels = new ArrayList<>();
            for (Action a : Action.actionList) {
                if(labels.contains(a.getName())){
                    continue;
                }
                if(a.getClassification().getName().equals(category)){
                    labels.add(a.getName());
                }
            }

            ArrayList<Integer> amounts = new ArrayList<>();
            for (String s : labels) {
                amounts.add(Action.getAmount(s));
            }

            makePieChart(category, labels, amounts, chart);



            return convertView;

        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_statistics, parent, false);
        TextView nameText = convertView.findViewById(R.id.nameOfChart);
        LineChart chart = convertView.findViewById(R.id.chart);

        nameText.setText(name);


        // Find amounts.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM yyyy", Locale.ENGLISH);

        ArrayList<Integer> amounts = new ArrayList<>();
        String date = dateFormat.format(new Date());
        int amount;
        for (int i = 0; i < 7; i++) {
            amount = 0;
            for (Action a : Action.getAllWithName(name)) {
                if (dateFormat.format(a.getDate()).equals(date)) {
                    // Date we look for.
                    amount++;
                }
            }

            // Increment the day looked at.
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(dateFormat.parse(date));
            } catch (ParseException e) {
                Toast.makeText(context, "Failed to generate statistics list!", Toast.LENGTH_LONG).show();
                return convertView;
            }
            c.add(Calendar.DATE, -1);  // number of days to add
            date = dateFormat.format(c.getTime());  // currShownDate is now the new date

            amounts.add(amount);
        }

        makeLineChart(name, amounts, chart);

        return convertView;
    }

    // If there is a reference to a chart, this will set all the values.
    private void makeLineChart(String title, ArrayList<Integer> amounts, LineChart lineChart) {
        Log.i("Test", "Title: " + title + ", Amounts: " + amounts + ", Chart: " + lineChart);
        // Creates the "chart" as in where it is placed. (A reference)
        lineChart.setData(Charts.createLineData(title, amounts));
        // Updates the chart with the new values.
        lineChart.invalidate();
        // A String of what labels should be on the x-axis. (in order of x1,x2,x3...)
        final String[] labels = new String[] {"D1", "D2", "D3", "D4", "D5", "D6", "D7"};
        // Sets the labels.
        Charts.getXAxisData(labels, lineChart);
    }

    // If there is a reference to a chart, this will set all the values.
    private void makePieChart(String title, ArrayList<String> names, ArrayList<Integer> amounts, PieChart pieChart) {
        int[] colors = new int[] { R.color.neonPink,
                R.color.green,
                R.color.blue,
                R.color.lightGreen,
                R.color.red,
                R.color.yellow,
                R.color.lightBlue,
                R.color.magenta,
                R.color.orange,
                R.color.turquoise,
                R.color.pumpkin,
                R.color.palePink,
                R.color.swamp,
                R.color.darkPurple
        };
        PieDataSet dataSet = new PieDataSet(Charts.createEntries(names, amounts), title);
        dataSet.setColors(colors, context);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh
    }

}
