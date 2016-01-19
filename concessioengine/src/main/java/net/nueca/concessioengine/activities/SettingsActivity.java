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
import java.util.List;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class SettingsActivity extends ImonggoAppCompatActivity {

    private ArrayList<AppSettings> appSettings = new ArrayList<AppSettings>(){{
        add(new AppSettings(true, AppSettings.AppSettingType.APPLICATION, null, 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Version: 1.1.1", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Clear transactions", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Clear cached doc", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Auto update app", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Version: 1.1.1", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Clear transactions", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Clear cached doc", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Auto update app", 0));
        add(new AppSettings(false, AppSettings.AppSettingType.APPLICATION, "Auto update app", 0)); // 9
        add(new AppSettings(true, AppSettings.AppSettingType.USER, null, 10));
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 11
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 12
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 13
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 14
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 15
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 16
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 17
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 18
        add(new AppSettings(false, AppSettings.AppSettingType.USER, "Sample", 10)); // 19
        add(new AppSettings(true, AppSettings.AppSettingType.LISTING, null, 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
        add(new AppSettings(false, AppSettings.AppSettingType.LISTING, "Sample", 20));
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

        return null;
    }
}
