package nueca.net.salesdashboard.tools;


import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Settings;

import java.sql.SQLException;

import nueca.net.salesdashboard.enums.FormatValue;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class ImonggoSettingsTools {


    public static String getFormatSettings(ImonggoDBHelper dbHelper, FormatValue formatValue) throws SQLException {

        Settings settings = getSettings(dbHelper, getNameString(formatValue));

        if(settings != null) {
            return settings.getValue();
        }

        return "";
    }


    public static Settings getSettings(ImonggoDBHelper dbHelper, String name) throws SQLException {
        return dbHelper.getSettings().queryBuilder().where().eq("name", name).queryForFirst();
    }

    public static String getNameString(FormatValue formatValue) {

        if(formatValue == FormatValue.FORMAT_UNIT) {
            return "format_unit";
        }

        if(formatValue == FormatValue.FORMAT_DECIMAL_SEPARATOR) {
            return "format_decimal_sep";
        }

        if(formatValue == FormatValue.FORMAT_NO_OF_DECIMALS) {
            return "format_no_of_decimals";
        }

        if(formatValue == FormatValue.FORMAT_THOUSANDS_SEPARATOR) {
            return "format_thousands_sep";
        }

        if(formatValue == FormatValue.FORMAT_POSTFIX_UNIT) {
            return "format_postfix_unit";
        }

        if(formatValue == FormatValue.FORMAT_ROUND_STYLE) {
            return "format_round_style";
        }

        if(formatValue == FormatValue.FORMAT_ROUND_VALUE) {
            return "format_round_value";
        }

        return "";
    }
}
