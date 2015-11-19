package net.nueca.concessioengine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;

/**
 * Created by rhymart on 11/19/15.
 */
public abstract class DashboardActivity extends ImonggoAppCompatActivity {

    private Class<?> nextActivityClass;

    protected void setNextActivityClass(Class<?> nextActivityClass) {
        this.nextActivityClass = nextActivityClass;
    }

    public void moduleSelected(View view) {
        ConcessioModule concessioModule = ConcessioModule.ORDERS;
        if(view.getTag() != null) {
            concessioModule = (ConcessioModule)view.getTag();
        }
        else {
            Log.e("Ooops!", "no tag for this button");
            return;
        }
        if(nextActivityClass == null) {
            Log.e("Ooops!", "Please define the proper next activity");
            return;
        }
        Intent intent = new Intent(this, nextActivityClass);
        Bundle bundle = addExtras(concessioModule);
        if(bundle != null)
            intent.putExtras(bundle);
        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, concessioModule.ordinal());
        startActivity(intent);
    }

    protected abstract Bundle addExtras(ConcessioModule concessioModule);
}
