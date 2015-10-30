package nueca.net.salesdashboard.tools;

import android.util.Log;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class DateTools {
    public static String TAG = "DateTools";

    public static List<String> getDatesOfThisWeek() {
        List<String> dateThisWeek;
        int thisWeekSize = 1;

        dateThisWeek = new ArrayList<>();

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        thisWeekSize = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < thisWeekSize; i++) {
            dateThisWeek.add(df.format(c.getTime()));
            //Log.e(TAG, "Saving day: " + df.format(c.getTime()));
            c.add(Calendar.DATE, 1);
        }
        return dateThisWeek;
    }


    public static List<String> getDatesOfLastWeek() {
        List<String> dateLastWeek = new ArrayList<>();

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to sunday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i <= 6; i++) {
            c.add(Calendar.DATE, -1);
            dateLastWeek.add(df.format(c.getTime()));
            //Log.e(TAG + " Last Week ", df.format(c.getTime()));
        }

        Collections.reverse(dateLastWeek);

        return dateLastWeek;
    }

    public static List<String> getDatesOfLast2Weeks() {
        List<String> dateLast2Week = new ArrayList<>();

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to sunday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i <= 13; i++) {
            c.add(Calendar.DATE, -1);
            if(i >= 7) {
                dateLast2Week.add(df.format(c.getTime()));
                //Log.e(TAG + " Last 2 Weeks: ", df.format(c.getTime()));
            }
        }

        Collections.reverse(dateLast2Week);

        return dateLast2Week;
    }

    public static List<String> getDatesOfLast3Weeks() {
        List<String> dateLast3Week = new ArrayList<>();

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to sunday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i <= 20; i++) {
            c.add(Calendar.DATE, -1);
            if(i >= 14) {
                dateLast3Week.add(df.format(c.getTime()));
                //Log.e(TAG + " Last 3 Weeks: ", df.format(c.getTime()));
            }
        }

        Collections.reverse(dateLast3Week);

        return dateLast3Week;
    }

    public static List<String> getDatesOfLast4Weeks() {
        List<String> dateLast4Week = new ArrayList<>();

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to sunday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i <= 27; i++) {
            c.add(Calendar.DATE, -1);
            if(i >= 21) {
                dateLast4Week.add(df.format(c.getTime()));
                //Log.e(TAG + " Last 4 Weeks: ", df.format(c.getTime()));
            }
        }

        Collections.reverse(dateLast4Week);

        return dateLast4Week;
    }

    public static List<String> getDatesThisMonth() {
        List<String> dateThisMonth = new ArrayList<>();

        int thisMonthSize = 1;

        // Get calendar set to current date and time
        Calendar c = Calendar.getInstance();

        // Set the calendar to sunday of the current week
        c.set(Calendar.DAY_OF_MONTH, Calendar.SUNDAY);

        thisMonthSize = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        Log.e(TAG, "This Month Size: " + thisMonthSize);

        // Print dates of the current week starting on Monday to Friday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < thisMonthSize; i++) {
            dateThisMonth.add(df.format(c.getTime()));

            //Log.e(TAG, df.format(c.getTime()));
            c.add(Calendar.DATE, 1);
        }
        return dateThisMonth;
    }

    public static String getTextViewDate(String date) {
        return getTextViewDate(date, "");
    }

    public static String getTextViewDate(String date, String day) {

        if(day != "") {
            return getMonth(date) + " " + getDay(date) +
                    "-" + getDay(day) + ", " + getYear(date) + " -";
        } else {
            return getMonth(date) + " " + getDay(date) +
                    "-" + getDay(date) + ", " + getYear(date) + " -";
        }
    }

    public static String getDay(String day) {
        String[] tokens = day.split("-");
        return tokens[2];
    }

    public static String getMonth(String date) {
        String[] tokens = date.split("-");
        return new DateFormatSymbols().getShortMonths()[Integer.parseInt(tokens[1]) - 1];
    }

    public static String getYear(String date) {
        String[] tokens = date.split("-");
        return tokens[0];
    }
}
