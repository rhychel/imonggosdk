package net.nueca.concessioengine.printer.epson.enums;

import android.util.Log;

/**
 * Created by Jn on 31/01/16.
 */
public enum EPSONPrinterSeries {
    t20("TM-T20", 6),
    m10("TM-m10", 0),
    m30("TM-m30", 1),
    p20("TM-P20", 2),
    p60("TM-P60 ", 3),
    p60ii("TM-P60II", 4),
    p80("TM-P80", 5),
    t70("TM-T70", 7),
    t81("TM-T81", 8),
    t82("TM-T82", 9),
    t83("TM-T83", 10),
    t88("TM-T88", 11),
    t90("TM-T90", 12),
    t90kp("TM-T90KP", 13),
    u220("TM-U220", 14),
    u330("TM-U330", 15),
    l90("TM-L90", 16),
    h6000("TM-H600", 17),
    none("None", -1);

    private static String TAG = "EPSONPrinterSeries";
    private String name;
    private int series;

    EPSONPrinterSeries(String name, int series) {
        this.series = series;
        this.name = name;
    }

    public static EPSONPrinterSeries getPrinterSeriesByName(String name) {
        for (EPSONPrinterSeries p : EPSONPrinterSeries.values()) {
            if (p.getName().equals(name)) {
                Log.e(TAG, "Found! " + p.getName());
                return p;
            }
        }
        return none;
    }

    public int getSeries() {
        return series;
    }

    public String getName() {
        return this.name + " Series";
    }

}
