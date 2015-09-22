package net.nueca.imonggosdk.tools;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by gama on 9/9/15.
 */
public class NumberTools {
    public static Double toDouble(String dblString) {
        if(dblString == null)
            return 0d;

        dblString = dblString.replaceAll("[^0-9.-]","");

        if(dblString.length() == 0)
            return 0d;

        return Double.parseDouble(dblString);
    }
    public static Double toNullableDouble(String dblString) {
        if(dblString == null)
            return null;

        dblString = dblString.replaceAll("[^0-9.-]","");

        if(dblString.length() == 0)
            return null;

        return Double.parseDouble(dblString);
    }
    public static BigDecimal toBigDecimal(String bigDecimalString) {
        if(bigDecimalString == null)
            return BigDecimal.ZERO;

        bigDecimalString = bigDecimalString.replaceAll("[^0-9.-]","");

        if(bigDecimalString.length() == 0)
            return BigDecimal.ZERO;

        return new BigDecimal(bigDecimalString);
    }

    public static String separateInCommas(String unparsedNum) {
        return separateInCommas(toBigDecimal(unparsedNum));
    }
    public static String separateInCommas(BigDecimal num) {
        return String.format("%,1.2f", num);
    }
    public static String separateInCommas(Double num) {
        return String.format("%,1.2f",num);
    }
    public static String separateInCommas(int num) {
        return String.format("%,1d",num);
    }

    public static final DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###,##0.##");
    public static String separateInCommasHideZeroDecimals(BigDecimal num) {
        return decimalFormat.format(num);
    }
    public static String separateInCommasHideZeroDecimals(Double num) {
        return decimalFormat.format(num);
    }
    public static String separateInCommasHideZeroDecimals(String num) {
        return decimalFormat.format(toBigDecimal(num));
    }
}
