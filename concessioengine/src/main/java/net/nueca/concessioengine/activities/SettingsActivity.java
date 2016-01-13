package net.nueca.concessioengine.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SettingsAdapter;
import net.nueca.concessioengine.objects.AppSettings;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;

import java.util.ArrayList;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class SettingsActivity extends ImonggoAppCompatActivity {

    private ArrayList<AppSettings> appSettings = new ArrayList<AppSettings>(){{
        add(new AppSettings(true, AppSettings.AppSettingType.APPLICATION, null, 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Sample1", 0));
    }};

    private RecyclerView rvCustomers;
    private Toolbar tbActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_customers_fragment_rv);
        rvCustomers = (RecyclerView) findViewById(R.id.rvCustomers);
        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);

        SettingsAdapter settingsAdapter = new SettingsAdapter(this, appSettings);
        settingsAdapter.initializeRecyclerView(this, rvCustomers);
        rvCustomers.setAdapter(settingsAdapter);

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
}
