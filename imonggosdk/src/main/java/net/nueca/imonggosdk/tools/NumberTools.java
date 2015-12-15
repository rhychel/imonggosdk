package net.nueca.imonggosdk.tools;

import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by gama on 9/9/15.
 */
public class NumberTools {

    // ADDED BY RHY
    public static boolean isNumberAnInteger(String number) {
        return number.matches("-?\\d+\\.([0]+)?") && !number.equals("");
    }

    public static boolean isNumber(String number) {
        return isNumberDouble(number) || isNumberInteger(number);
    }

    public static boolean isNumberInteger(String number) {
        return number.matches("-?\\d+") && !number.equals("");
    }

    public static boolean isNumberDouble(String number) {
        return number.matches("-?\\d+\\.(\\d+)?") && !number.equals("");
    }

    public static boolean isDecimalLimitReached(String number, int decimalLimit) {
        return !number.matches("-?\\d+\\.(\\d{1,"+decimalLimit+"})?") && !number.equals("");
    }

    public static boolean isNumberLimitReached(String number, int numberLimit) {
        return !number.matches("-?(\\d{1,"+numberLimit+"})?(\\.(\\d{1,2})?)?");
    }

    public static boolean isTrailingZero(String number) {
        return number.equals("0") && number.length() == 1;
    }

    public static void correctNumber(EditText etNumber, String oldNumber, String newNumber,
                                     int numberLimit, int decimalLimit) {
        if(NumberTools.isTrailingZero(newNumber)) {
            etNumber.setText("");
            return;
        }
        if(NumberTools.isNumberLimitReached(newNumber, numberLimit))
            etNumber.setText(oldNumber);
        else if(!newNumber.equals("")){
            if (newNumber.lastIndexOf(".") > -1) {
                if(newNumber.equals("."))
                    etNumber.setText("0.");
                else if (NumberTools.isDecimalLimitReached(newNumber, decimalLimit))
                    etNumber.setText(oldNumber);
                else {
                    if(newNumber.equals("."))
                        etNumber.setText("0.");
                    else if (!NumberTools.isNumberDouble(newNumber))
                        etNumber.setText(oldNumber);
                }
            } else {
                if (!NumberTools.isNumberInteger(newNumber))
                    etNumber.setText(oldNumber);
            }
        }
    }
    // -----------------------------------------------

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
        return String.format("%,1.2f", num);
    }
    public static String separateInCommas(int num) {
        return String.format("%,1d", num);
    }

    public static String separateInSpaces(String unparsedNum) {
        return separateInCommas(unparsedNum).replace(',', ' ');
    }
    public static String separateInSpaces(BigDecimal num) {
        return separateInCommas(num).replace(',', ' ');
    }
    public static String separateInSpaces(Double num) {
        return separateInCommas(num).replace(',', ' ');
    }
    public static String separateInSpaces(int num) {
        return separateInCommas(num).replace(',', ' ');
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

    public static String separateInSpaceHideZeroDecimals(BigDecimal num) {
        return separateInCommasHideZeroDecimals(num).replace(',', ' ');
    }
    public static String separateInSpaceHideZeroDecimals(Double num) {
        return separateInCommasHideZeroDecimals(num).replace(',', ' ');
    }
    public static String separateInSpaceHideZeroDecimals(String num) {
        return separateInCommasHideZeroDecimals(toBigDecimal(num)).replace(',', ' ');
    }
}
