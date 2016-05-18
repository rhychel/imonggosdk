package net.nueca.imonggosdk.tools;

import android.util.Log;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.document.Document;

import java.sql.SQLException;

/**
 * Created by rhymartmanchus on 18/05/2016.
 */
public class DataCheckerTools {

    public static void queryAllDocuments(ImonggoDBHelper2 dbHelper) {
        boolean hasOne = false;
        for(Document document : Document.fetchAll(dbHelper, Document.class)) {
            hasOne = true;
            Log.e("Document", document.getReference()+" | docLines = "+document.getDocument_lines().size()
                    + " | target_branch_id = "+document.getTarget_branch_id()+ " | branch_id = "+document.getBranch_id());
        }
        if(!hasOne)
            Log.e("Document", "no documents are saved!");
    }

    public static void viaTargetBranchId(ImonggoDBHelper2 dbHelper, int target_branch_id, String reference) throws SQLException {
        QueryBuilder<Document, Integer> queryBuilder = dbHelper.fetchObjectsInt(Document.class).queryBuilder();

        Where<Document, Integer> whereDoc = queryBuilder.where();
        whereDoc.eq("target_branch_id", target_branch_id).and();
        whereDoc.eq("reference", reference);

        Document document = queryBuilder.queryForFirst();
        if(document == null)
            Log.e("Document via TBI", "Failed!");
        else
            Log.e("Document via TBI", document.getReference());
    }

    public static void viaBranchId(ImonggoDBHelper2 dbHelper, int branch_id, String reference) throws SQLException {
        QueryBuilder<Document, Integer> queryBuilder = dbHelper.fetchObjectsInt(Document.class).queryBuilder();

        Where<Document, Integer> whereDoc = queryBuilder.where();
        whereDoc.eq("branch_id", branch_id).and();
        whereDoc.eq("reference", reference);

        Document document = queryBuilder.queryForFirst();
        if(document == null)
            Log.e("Document via BI", "Failed!");
        else
            Log.e("Document via BI", document.getReference());
    }

}
