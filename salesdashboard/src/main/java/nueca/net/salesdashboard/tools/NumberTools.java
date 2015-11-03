package nueca.net.salesdashboard.tools;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class NumberTools {

    public static String RoundValue(Double num, int type) {
        if (type == 0) {

            if (num == 0) {
                return "0";
            } else {
                DecimalFormat formatter;
                if ((num % 1) == 0) {
                    formatter = new DecimalFormat("#,##0");
                } else {
                    formatter = new DecimalFormat("#,##0.00");
                }

                formatter.setRoundingMode(RoundingMode.HALF_UP);
                formatter.setDecimalSeparatorAlwaysShown(false);
                return formatter.format(num);
            }
        } else if (type == 1) {
            DecimalFormat formatter = new DecimalFormat("0");
            formatter.setRoundingMode(RoundingMode.UP);
            return formatter.format(num);
        } else if (type == 2) {
            if (num == 0) {
                return "0.00";
            } else {
                DecimalFormat formatter;

                formatter = new DecimalFormat("#,##0.00");

                formatter.setRoundingMode(RoundingMode.HALF_UP);
                formatter.setDecimalSeparatorAlwaysShown(false);
                return formatter.format(num);
            }
        }
        return "0";
    }
}