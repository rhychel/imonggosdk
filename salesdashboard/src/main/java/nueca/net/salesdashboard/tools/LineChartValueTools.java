package nueca.net.salesdashboard.tools;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Pattern;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class LineChartValueTools {
    public static String TAG = "CurrencyTools";

    public static String getShortenedCurrency(String number) {
        Pattern pThousand = Pattern.compile("^(\\d{4,6})(.\\d*)?$");
        Pattern pMillion = Pattern.compile("^(\\d{7,9})(.\\d*)?$");
        Pattern pBillion = Pattern.compile("^(\\d{10,12})(.\\d*)?$");
        Pattern pTrillion = Pattern.compile("^(\\d{13,15})(.\\d*)?$");
        Pattern pQuad = Pattern.compile("^(\\d{16,18})(.\\d*)?$");

        Double value = Double.parseDouble(number);
        Log.e(TAG, "value: " + value);
        if(pQuad.matcher(number).matches()) {
            return "$" + (int) (value / 1000000000000000.0000) + "Q";
        } else if(pTrillion.matcher(number).matches()) {
            return "$" +  (int) (value / 1000000000000.00) + "T";
        } else if(pBillion.matcher(number).matches()) {
            return "$" + (int) (value / 1000000000) + "B";
        } else if(pMillion.matcher(number).matches()) {
            return "$" + (int) (value / 1000000) + "M";
        } else if(pThousand.matcher(number).matches()) {
            return "$" + (int) (value / 1000) + "K";
        } else {
            return "$" + (int) Math.ceil(value);
        }
    }

    public static String getShortenedCurrency(float value) {

        if (value / 100 < 10) {
            return "$" +  (int) value + "";
        } else if ((value / 1000.00) < 1000.00000) {
            return "$" + (int) (value/ 1000) + "K";
        } else if ((value / 1000000.00) < 1000.00000) {
            return "$" +  (int) (value/ 1000000.00) + "M";
        } else if ((value / 1000000000) < 1000.00000 ) {
            return  "$" + (int) (value/ 1000000000) + "B";
        } else if ((value / 1000000000000.00) < 1000.00000) {
            return "$" + (int) (value/ 1000000000000.00) + "T";
        } else if ((value / 1000000000000000.00) < 1000.00000) {
            return "$" + (int) (value/ 1000000000000000.00) + "QUAD";
        }
        return "";
    }


    public static int computedMaxValue(int value) {
        double val = value;
        double div = 10;

        while (val > 10) {
            div *= 10.0;
            val = value / div;
        }

        val = ((int) Math.ceil(val) % 2 == 0) ? Math.ceil(val) : Math.ceil(val) + 1.0;

        int newMax = (int) (Math.ceil(val) * div);

        return newMax < 8 ? 8 : newMax;
    }

    public static String computedMaxValue(double value) {
        double val = value;
        long div = 1;

        while (val > 10) {
            div *= 10.0;
            val = value / div;
        }

        val = ((int) Math.ceil(val) % 2 == 0) ? Math.ceil(val) : Math.ceil(val) + 1.0;

        long newMax = (long) (Math.ceil(val) * div);

        if(newMax < 4)
            return "4";
        if(newMax < 8)
            return "8";

        return newMax + "";
    }


    public static Double add10percent(double value) {
        return value + (value / 10);
    }

    public static Long add10percentLong(long value) {
        return value + (value / 10);
    }

    public static String convertFromScientificNotation(double number) {
        // Check if in scientific notation
        if (String.valueOf(number).toLowerCase().contains("e")) {
            System.out.println("The scientific notation number'"
                    + number
                    + "' detected, it will be converted to normal representation with 25 maximum fraction digits.");
            NumberFormat formatter = new DecimalFormat();

            formatter.setMaximumFractionDigits(25);
            return formatter.format(number);
        } else
            return String.valueOf((int)number);
    }
}

