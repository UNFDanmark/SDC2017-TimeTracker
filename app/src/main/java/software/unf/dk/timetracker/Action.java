package software.unf.dk.timetracker;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/***
 * Class for handling user activities
 */
class Action {
    public static ArrayList<Action> actionList = new ArrayList<>();

    private String name;
    private Classification classification;
    // TODO: implement date/time modification frontend
    private Date date;
    private String description;

    private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);

    public Action(String name, Classification classification, Date date) {
        this.name = name;
        this.classification = classification;
        this.date = date;
    }
    public Action(String name, Classification classification, Date date, String description) {
        this.name = name;
        this.classification = classification;
        this.date = date;
        this.description = description;
    }

    /**
     * Getter and setters.
     */
    // Name.
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Classification.
    public Classification getClassification() {
        return classification;
    }
    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    // Dates and time.
    public Date getDate() {
        return date;
    }
    static String dateToString(Date date) {
        return DATETIME_FORMAT.format(date);
    }
    static Date stringToDate(String string) {
        try {
            return DATETIME_FORMAT.parse(string);
        } catch (ParseException e){
            Log.e("Error", "Failed to parse date string.");
            return null;
        }
    }

    // Description
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    static ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Action a : actionList) {
            if (!names.contains(a.getName())) names.add(a.getName());
        }
        return names;
    }
    /*static ArrayList<String> getNames(String classification) {
        ArrayList<String> names = new ArrayList<>();
        for (Action a : actionList) {
            if (!names.contains(a.getName()) && a.getClassification().getName().equals(classification))
                names.add(a.getName());
        }
        return names;
    }*/
    static int getAmount(String name) {
        int i = 0;
        for (Action a : actionList) {
            if (a.getName().equals(name)) i++;
        }
        return i;
    }
    static ArrayList<Action> getAllWithName(String name) {
        ArrayList<Action> list = new ArrayList<>();
        for (Action a : actionList) {
            if (a.getName().equals(name)) list.add(a);
        }
        return list;
    }

    // Parcel functions
    /*protected Action(Parcel in) {
        name = in.readString();
        classification = (Classification) in.readValue(Classification.class.getClassLoader());
        long dateLong = in.readLong();
        date = dateLong != -1 ? new Date(dateLong) : null;
        if (date == null) date = new Date();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeValue(classification);
        out.writeLong(date != null ? date.getTime() : -1L);
    }

    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };*/
}
