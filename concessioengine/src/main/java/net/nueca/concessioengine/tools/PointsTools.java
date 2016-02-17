package net.nueca.concessioengine.tools;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 17/02/2016.
 */
public class PointsTools {
    public static SalesPromotion getPointSalesPromotion(ImonggoDBHelper2 helper) throws SQLException {
        List<SalesPromotion> salesPromotions = helper.fetchObjects(SalesPromotion.class).queryBuilder().where()
                .like("promotion_type_name", "point").and()
                .like("status","A").or().isNull("status").query();
        for(SalesPromotion salesPromotion : salesPromotions) {
            if(DateTimeTools.isNowBetween(salesPromotion.getFromDate(),salesPromotion.getToDate()) &&
                    salesPromotion.getSettings() != null)
                return salesPromotion;
        }
        return null;
    }

    public static double amountToPoints(SalesPushSettings salesPushSettings, double amount) {
        if(salesPushSettings.getConversion_to_currency() == 0d)
            return 0d;
        return amount / salesPushSettings.getConversion_to_currency();
    }

    public static double pointsToAmount(SalesPushSettings salesPushSettings, double points) {
        Log.e("Points To Peso", points + " * " + salesPushSettings.getConversion_to_currency());
        return points * salesPushSettings.getConversion_to_currency();
    }
}
