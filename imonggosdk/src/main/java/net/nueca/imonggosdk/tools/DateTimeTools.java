package net.nueca.imonggosdk.tools;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class DateTimeTools {

    /**
     *
     * Get current date time formatted as yyyy-MM-dd HH:mm:ss splitted by space.
     *
     * e.g.: 2013-06-14 11:18:20
     *
     * @return string array where index 0 is the date, and index 1 is the time.
     */
    public static String[] getCurrentDateTimeInvoice() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date currentDateTime= new Date();
        String[] invoiceDate = dateFormat.format(currentDateTime).split(" ");
        return invoiceDate;
    }

    /**
     *
     * Get current date time formatted as MMMMM dd, yyyy/h:mm a splitted by slash( <b>/</b> ).
     *
     * e.g. June 14, 2013/11:25 AM
     *
     * @return string array where index 0 is the date, and index 1 is the time.
     */
    public static String getCurrentDateTime(boolean withAt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy/h:mm a");
        Date currentDateTime= new Date();
        String[] invoiceDate = dateFormat.format(currentDateTime).split("/");
        String datetime = invoiceDate[0]+((withAt) ? ", " : "\n")+invoiceDate[1];
        return datetime;
    }

    /**
     *
     * Convert the string date time formatted as yyyy-MM-ddTHH:mm:ssZ (where T and Z are characters, not pattern) to date
     * and return as a string formatted as defined on the second parameter.
     *
     * e.g. Input: "2013-05-05T00:00:00Z" - Output: (format to 'As of May, 2013')
     * @param dateTime
     * @param formatForDisplay
     * @return
     */
    public static String convertToDate(String dateTime, String formatForDisplay) {
        String []parseDateTime = dateTime.split("T");
        String finalDateTime = parseDateTime[0]+" "+parseDateTime[1].replace("Z", "");
        SimpleDateFormat convertStringToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat forDisplaying = new SimpleDateFormat(formatForDisplay);
        try {
            Date theDate = convertStringToDate.parse(finalDateTime);
            String result = forDisplaying.format(theDate);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * Convert the string date time formatted as yyyy-MM-ddTHH:mm:ssZ (where T and Z are characters, not pattern) to date
     * and return as a string formatted as defined on the second parameter.
     *
     * e.g. Input: "2013-05-05T00:00:00Z" - Output: (format to 'As of May, 2013')
     * @param dateTime
     * @param formatForDisplay
     * @return
     */
    public static String convertToDate(String dateTime, String inputFormat, String formatForDisplay) {
        SimpleDateFormat convertStringToDate = new SimpleDateFormat(inputFormat);
        SimpleDateFormat forDisplaying = new SimpleDateFormat(formatForDisplay);
        try {
            Date theDate = convertStringToDate.parse(dateTime);
            String result = forDisplaying.format(theDate);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * Convert the string date time formatted as yyyy-MM-ddTHH:mm:ssZ (where T and Z are characters, not pattern) to date
     * and return as a string formatted.
     *
     * e.g. Input: "2013/05/05 00:00:00 +0000" - Output: (format to '2013-05-05T00:00:00Z')
     * @param dateTime
     * @return
     */
    public static String convertDateForUrl(String dateTime) {
        SimpleDateFormat convertStringToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
        SimpleDateFormat forDisplaying = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Log.e("Timezone is => ", convertStringToDate.getTimeZone().getID() + " --- " + dateTime);
        forDisplaying.setTimeZone(convertStringToDate.getTimeZone());
        forDisplaying.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date theDate = convertStringToDate.parse(dateTime);
            String result = forDisplaying.format(theDate);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * Get current date time formatted as with the passed parameter.
     * <br/>
     * e.g.: <br/>
     * parameter: EEE, MMM. dd, yyyy hh:mm aaa<br/>
     * output: Thu, Jan. 28, 2013 24:28PM
     *
     * @return formatted current date string
     */
    public static String getCurrentDateTimeWithFormat(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date currentDateTime= new Date();
        String currentDate = dateFormat.format(currentDateTime);
        return currentDate;
    }

    /**
     *
     * Get current date time formatted as with the passed parameter.
     * <br/>
     * e.g.: <br/>
     * parameter: EEE, MMM. dd, yyyy hh:mm aaa<br/>
     * output: Thu, Jan. 28, 2013 24:28PM
     *
     * @return formatted current date string
     */
    public static String getCurrentDateTimeOnOffsetWithFormat(String format, int offsetField, int offsetValue) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        currentTime.add(offsetField, offsetValue);
        String currentDate = dateFormat.format(currentTime.getTime());
        return currentDate;
    }

    /**
     *
     * Get current date time formatted as with the passed parameter.
     * <br/>
     * e.g.: <br/>
     * parameter: EEE, MMM. dd, yyyy hh:mm aaa<br/>
     * output: Thu, Jan. 28, 2013 24:28PM
     *
     * @return formatted current date string
     */
    public static String getCurrentDateTimeOnOffsetWithFormatGMT(String format, int offsetField, int offsetValue) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        currentTime.add(offsetField, offsetValue);
        String currentDate = dateFormat.format(currentTime.getTime());
        return currentDate;
    }

    /**
     *
     * Get current date time in a UTC format.
     * <br/>
     * e.g.: <br/>
     * format: yyyy-MM-dd HH:mm:ss Z<br/>
     * output: 2013-09-01 13:15:59 +0800
     *
     * @return UTC formatted current date string
     */
    public static String getCurrentDateTimeUTCFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        Date currentDateTime= new Date();
        String currentDate = dateFormat.format(currentDateTime);
        return currentDate;
    }

    /**
     *
     * Get current date time in a UTC 0:00 format.
     * <br/>
     * e.g.: <br/>
     * format: yyyy-MM-dd HH:mm:ss Z<br/>
     * output: 2013-09-01 13:15:59 +0000
     *
     * @return UTC formatted current date string
     */
    public static String getCurrentDateTimeUTC0Format() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date currentDateTime= new Date();
        String currentDate = dateFormat.format(currentDateTime);
        return currentDate;
    }

}
