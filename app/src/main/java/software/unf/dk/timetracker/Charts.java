package software.unf.dk.timetracker;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class Charts {


    /**
     * Pie Chart.
     *
     * Code for implementing it in an Instance.

     // If there is a reference to a chart, this will set all the values.
     private void makePieChart(String title, ArrayList<String> names, ArrayList<Integer> amounts, PieChart pieChart) {
         int[] colors = new int[] { R.color.neonpink, R.color.green, R.color.blue, R.color.lightgreen, R.color.red, R.color.yellow, R.color.lightblue, R.color.magenza, R.color.orange, R.color.turqoise, R.color.pumpkin, R.color.palepink, R.color.svump, R.color.darkpurple }, getApplicationContext;
         PieDataSet dataSet = new PieDataSet(Charts.createEntries(names, amounts), title);
         dataSet.setColors(colors, this);
         PieData data = new PieData(dataSet);
         pieChart.setData(data);
         pieChart.invalidate(); // refresh
     }

     *
     */
    public static List<PieEntry> createEntries(ArrayList<String> names, ArrayList<Integer> amounts) {
        List<PieEntry> result = new ArrayList<>();
        if(amounts.size() != names.size()) {
            throw new IllegalArgumentException("List of either amounts or names is not equal to each other and can there fore not be handled");
        }
        for (int i = 0; i < names.size(); i++) {
            PieEntry entry = new PieEntry(amounts.get(i), names.get(i));
            result.add(entry);
        }

        return result;
    }




    // Creates all the data for a given line chart.
    static LineData createLineData(String title, ArrayList<Integer> amounts){
        LineDataSet dataSet = new LineDataSet(createEntries(amounts), title);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        return new LineData(dataSet);
    }

    // Assigns the labels for the x-axis.
    static XAxis getXAxisData(String[] names, LineChart lineChart){
        // the labels that should be drawn on the XAxis
        final String[] labels = names;

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels[(int) value];
            }

            // we don't draw numbers, so no decimal digits needed
            //public int getDecimalDigits() {  return 0; }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        return xAxis;
    }

    // Formats the list of column values to a list of Entries, with the createLineData can use.
    private static List<Entry> createEntries(ArrayList<Integer> amounts) {
        List<Entry> result = new ArrayList<>();

        for (int i = 0; i < amounts.size(); i++) {
            Entry entry = new Entry((float) i, (float) amounts.get(i));
            result.add(entry);
        }

        return result;
    }
}