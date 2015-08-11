package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 8/11/15.
 */
public abstract class BaseCustomersFragment extends ImonggoFragment {
    protected static final long LIMIT = 100l;
    protected long offset = 0l;

    private String searchKey;

    protected ListView lvCustomers;
    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;

    protected abstract void toggleNoItems(String msg, boolean show);

    protected List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();

        boolean hasSearchKey = searchKey != null && !searchKey.isEmpty();

        try {
            Log.e("CUSTOMERS",getHelper().getCustomers().queryForAll().size()+"");
            if(hasSearchKey) {
                Where<Customer, Integer> whereCustomers = getHelper().getCustomers().queryBuilder().where();
                whereCustomers.like("name", "%" + searchKey + "%");

                QueryBuilder<Customer, Integer> resultCustomers = getHelper().getCustomers().queryBuilder()
                        .orderByRaw("name COLLATE NOCASE ASC").limit(LIMIT).offset(offset);
                resultCustomers.setWhere(whereCustomers);

                customers = resultCustomers.query();
            }
            else
                customers = getHelper().getCustomers().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    protected void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public SetupActionBar getSetupActionBar() {
        return setupActionBar;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public Toolbar getActionBar() {
        return tbActionBar;
    }

    public void setActionBar(Toolbar tbActionBar) {
        this.tbActionBar = tbActionBar;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }
}
