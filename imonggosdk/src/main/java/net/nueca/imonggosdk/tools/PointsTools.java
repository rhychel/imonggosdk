package net.nueca.imonggosdk.tools;

import android.util.Log;

import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gama on 17/02/2016.
 */
public class PointsTools {
    public static SalesPromotion getPointSalesPromotion(ImonggoDBHelper2 helper) throws SQLException {
        List<SalesPromotion> salesPromotions = helper.fetchObjects(SalesPromotion.class).queryBuilder()
                .orderBy("id", false).where()
                .like("promotion_type_name", "point").and()
                .like("status","A").or().isNull("status").query();
        for(SalesPromotion salesPromotion : salesPromotions) {
            if(DateTimeTools.isNowBetween(salesPromotion.getFromDate(),salesPromotion.getToDate()) &&
                    salesPromotion.getSettings() != null)
                return salesPromotion;
        }
        return null;
    }

    public static double pointsToAmount(SalesPushSettings salesPushSettings, double points) {
        if(salesPushSettings.getConversion_to_currency() == 0d)
            return 0d;
        return points / salesPushSettings.getConversion_to_currency();
    }

    public static double amountToPoints(SalesPushSettings salesPushSettings, double amount) {
        return amount * salesPushSettings.getConversion_to_currency();
    }

    public static PaymentType getRewardsPointsPaymentType(ImonggoDBHelper2 helper) {
        PaymentType points = null;
        try {
            Where<PaymentType, ?> where = helper.fetchObjects(PaymentType.class).queryBuilder().where();
            List<PaymentType> p = helper.fetchObjects(PaymentType.class).query(where.like("name", "%point%").or()
                    .like("name", "%rewards%").prepare());
            if(p != null && p.size() != 0)
                points = p.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return points;
    }

    public static double getTotalPointsInAmount(Invoice invoice, PaymentType rewardsPaymentType) {
        double totalPointsInAmount = 0d;
        for(InvoicePayment payment : invoice.getPayments()) {
            if(payment.getPayment_type_id() == rewardsPaymentType.getId())
                totalPointsInAmount += payment.getAmount();
        }
        return totalPointsInAmount;
    }
}
