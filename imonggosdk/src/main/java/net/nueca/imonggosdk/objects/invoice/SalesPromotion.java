package net.nueca.imonggosdk.objects.invoice;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by rhymart on 10/14/15.
 * imonggosdk2 (c)2015
 */
@DatabaseTable
public class SalesPromotion {
    @DatabaseField(id=true)
    private int id = 0;

    @DatabaseField
    private String settings = "", promotion_type_name = "", utc_created_at = "", utc_updated_at = "", status = null,
            name = "", code = "", photos_ids = "", to_date = "", from_date = "";

    @DatabaseField
    private transient Date toDate, fromDate;
}
