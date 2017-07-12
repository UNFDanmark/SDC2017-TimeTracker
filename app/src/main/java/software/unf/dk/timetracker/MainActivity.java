package software.unf.dk.timetracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // datafile names
    private final String ACTIONS_FILENAME = "actions.xml";
    private final String CLASSIFICATIONS_FILENAME = "classifications.xml";

    private static Integer notificationTime = 45; // Time in minutes until next notification.
    private static Boolean wantNotification = true; // If the user has enabled notifications

    private AutoCompleteTextView actionText;

    private EditText classificationText;
    private Spinner classificationSpinner;
    private String[] classificationSpinnerStrings;
    private String classificationString;


    public static void setWantNotification(Boolean wantNotification) {
        MainActivity.wantNotification = wantNotification;
    }
    public static void setNotificationTime(Integer notificationTime) {
        MainActivity.notificationTime = notificationTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //test();

        loadData();
        layoutSetup();
    }

    // Load the action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    // Handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                historyActivity(null);
                return true;
            case R.id.action_settings:
                settingsActivity(null);
                return true;
            case R.id.action_statistics:
                statisticsActivity(null);
                return true;
            default:
                // Unrecognized action, make superclass handle it
                return super.onOptionsItemSelected(item);
        }
    }

    // Used for testing ONLY!
    /*private void test(){
        // Delete data files
        File file = new File(getFilesDir(), CLASSIFICATIONS_FILENAME);
        file.delete();

        file = new File(getFilesDir(), ACTIONS_FILENAME);
        file.delete();
    }*/

    protected void onResume(){
        super.onResume();
        setSpinner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
        if (wantNotification) {
            new CountDownTimer(notificationTime *60*1000,1000){
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    createNotification();
                }
            }.start();
        }
    }

    /**
     * Read Classification and Action data from XML files
     */
    private void loadData() {
        // Open classifications file and read data
        ClassificationIOHandler classificationIOHandler = new ClassificationIOHandler(new File(getFilesDir(), CLASSIFICATIONS_FILENAME));
        Classification.classificationMap = Classification.listToMap(classificationIOHandler.parseClassifications());
        // Open actions file and read data
        ActionIOHandler actionIOHandler = new ActionIOHandler(new File(getFilesDir(), ACTIONS_FILENAME));
        Action.actionList = actionIOHandler.parseActions();
    }

    /**
     * Write Classification and Action data to XML files
     */
    private void saveData() {
        // Open classifications file and write data
        ClassificationIOHandler classificationIOHandler = new ClassificationIOHandler(new File(getFilesDir(), CLASSIFICATIONS_FILENAME));
        classificationIOHandler.writeClassifications(Classification.mapToList(Classification.classificationMap));
        // Open actions file and write data
        ActionIOHandler actionIOHandler = new ActionIOHandler(new File(getFilesDir(), ACTIONS_FILENAME));
        actionIOHandler.writeActions(Action.actionList);
    }

    /**
     * Assign layout variables and initialize elements
     */
    private void layoutSetup() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Actions.
        actionText = (AutoCompleteTextView) findViewById(R.id.actionText);
        // Dropdown.
        classificationSpinner = (Spinner)findViewById(R.id.spinner);
        classificationText = (EditText) findViewById(R.id.classificationText);

        setCompletion();
        setSpinner();
    }

    void setCompletion() {
        String[] actionNames = Action.getNames().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, actionNames);
        actionText.setAdapter(adapter);
    }

    private void setSpinner(){
        classificationSpinnerStrings = Classification.mapToStringList(Classification.classificationMap).toArray(new String[0]);

        // Doing so the Array can be put into the Spinner
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, classificationSpinnerStrings);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classificationSpinner.setAdapter(adapter);
        // Listen to things happens on the Spinner
        classificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Set string to selected classificationSpinner value
                classificationString = classificationSpinnerStrings[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        }
        );
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Runs when Enter is pressed
    public void enter(View view){
        // Creates new instance of an action and adds it to the list of actions.
        String name = actionText.getText().toString();
        if (name.equals("")) {
            showToast("Name can't be empty!");
            return;
        }
        actionText.setText("");
        setCompletion();
        Classification classification = Classification.getClassificationByName(classificationString);
        Log.e("Test", classification + "");
        Date date = new Date();
        Action.actionList.add(new Action(name, classification, date));
    }

    public void createClassification(View view){
        String name = classificationText.getText().toString();

        //Safety checks
        if (name.equals("")) {
            showToast("Name can't be empty");
            return;
        }
        if (!Classification.createNew(name)) {
            setSpinner();
            Classification c;
            if ((c = Classification.getClassificationByName(name)) != null) {
                if (!c.isVisible()) {
                    showToast("Category already exists!");
                    return;
                } else {
                    classificationText.setText("");
                }
            }
            return;
        }

        setSpinner();
        classificationText.setText("");
    }

    public void historyActivity(View view){
        Intent hisintent = new Intent(this, HistoryActivity.class);
        startActivity(hisintent);
    }

    public void settingsActivity(View view){
        Intent hissettingpath = new Intent(this, SettingsActivity.class);
        startActivity(hissettingpath);
    }

    public void statisticsActivity(View view){
        Intent hisppath = new Intent(this, StatisticsActivity.class);
        startActivity(hisppath);
    }

    public void createNotification (){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("TimeTracker")
                .setContentText("What are you doing?");
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(3, mBuilder.build());
    }

}
