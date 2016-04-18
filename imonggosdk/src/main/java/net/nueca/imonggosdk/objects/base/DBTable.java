package net.nueca.imonggosdk.objects.base;

import android.content.Intent;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/16/15.
 */
public abstract class DBTable {
    public static final String TAG = "DBTable";

    // NEW --------
    public abstract void insertTo(ImonggoDBHelper2 dbHelper);
    public abstract void deleteTo(ImonggoDBHelper2 dbHelper);
    public abstract void updateTo(ImonggoDBHelper2 dbHelper);
    public void dbOperation(ImonggoDBHelper2 dbHelper, DatabaseOperation databaseOperation) {
        if(databaseOperation == DatabaseOperation.INSERT) {
           // Log.e(TAG, "Inserting to database");
            insertTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.UPDATE) {
            //Log.e(TAG, "Updating to database");
            updateTo(dbHelper);
        } else if(databaseOperation == DatabaseOperation.DELETE) {
            //Log.e(TAG, "Deleting to database");
            deleteTo(dbHelper);
        }
    }

    public static <T> T fetchById(ImonggoDBHelper2 dbHelper, Class<T> c, Integer id) {
        try {
            if(id == null)
                return null;
            return dbHelper.fetchIntId(c).queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fetchById(ImonggoDBHelper2 dbHelper, Class<T> c, String id) {
        try {
            if(id == null)
                return null;
            return dbHelper.fetchStrId(c).queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> fetchAll(ImonggoDBHelper2 dbHelper, Class<T> c) {
        try {
            return dbHelper.fetchObjectsList(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> List<T> fetchAll(ImonggoDBHelper2 dbHelper, Class<T> c, long limit, long offset) {
        try {
            return dbHelper.fetchObjects(c).queryBuilder().limit(limit).offset(offset).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T extends DBTable> boolean update(ImonggoDBHelper2 dbHelper, Class<T> c, T obj) {
        try {
            return dbHelper.update(c, obj) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static <T extends DBTable> boolean delete(ImonggoDBHelper2 dbHelper, Class<T> c, T obj) {
        try {
            return dbHelper.delete(c, obj) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static <T extends DBTable> boolean create(ImonggoDBHelper2 dbHelper, Class<T> c, T obj) {
        try {
            return dbHelper.insert(c, obj) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // For convenience testing :)
    public interface ConditionsWindow<T, ID> {
        Where<T, ID> renderConditions(Where<T, ID> where) throws SQLException;
    }

    public static <T> List<T> fetchWithConditionInt(ImonggoDBHelper2 dbHelper, Class<T> c, ConditionsWindow<T, Integer> conditions) {
        return fetchWithConditionInt(dbHelper, c, conditions, null, null);
    }

    public static <T> List<T> fetchWithConditionInt(ImonggoDBHelper2 dbHelper, Class<T> c, ConditionsWindow<T, Integer> conditions, Long limit, Long offset) {
        try {
            QueryBuilder<T, Integer> queryBuilder = dbHelper.fetchObjectsInt(c).queryBuilder();
            queryBuilder.setWhere(conditions.renderConditions(queryBuilder.where()));
            return queryBuilder.offset(offset).limit(limit).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static <T> List<T> fetchWithConditionStr(ImonggoDBHelper2 dbHelper, Class<T> c, ConditionsWindow<T, String> conditions) {
        return fetchWithConditionStr(dbHelper, c, conditions, null, null);
    }

    public static <T> List<T> fetchWithConditionStr(ImonggoDBHelper2 dbHelper, Class<T> c, ConditionsWindow<T, String> conditions, Long limit, Long offset) {
        try {
            QueryBuilder<T, String> queryBuilder = dbHelper.fetchObjectsStr(c).queryBuilder();
            queryBuilder.setWhere(conditions.renderConditions(queryBuilder.where()));
            return queryBuilder.offset(offset).limit(limit).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
