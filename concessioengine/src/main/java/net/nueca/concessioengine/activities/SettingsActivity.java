package net.nueca.concessioengine.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.SettingsAdapter;
import net.nueca.concessioengine.tools.appsettings.AppSettings;
import net.nueca.concessioengine.tools.appsettings.AppTools;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class SettingsActivity extends ModuleActivity {

    private RecyclerView rvCustomers;
    private Toolbar tbActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_customers_fragment_rv);
        rvCustomers = (RecyclerView) findViewById(R.id.rvCustomers);
        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);

        SettingsAdapter settingsAdapter = new SettingsAdapter(this, generateSettings());
        settingsAdapter.initializeRecyclerView(this, rvCustomers);
        rvCustomers.setAdapter(settingsAdapter);

        rvCustomers.setBackgroundResource(android.R.color.white);

        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Settings");
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private List<AppSettings> generateSettings() {
        ArrayList<AppSettings> appSettings = new ArrayList<>();
        AppSettings appSetting = new AppSettings();
        appSetting.setHeader(true);
        appSetting.setSectionFirstPosition(0);
        appSetting.setAppSettingEntry(AppSettings.AppSettingEntry.VERSION);
        appSetting.setValue(AppTools.getAppVersionName(this));
        appSetting.setConcessioModule(ConcessioModule.APPLICATION);

        return appSettings;
    }
}
