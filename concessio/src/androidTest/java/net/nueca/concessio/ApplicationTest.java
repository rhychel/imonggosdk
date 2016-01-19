package net.nueca.concessio;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import junit.framework.TestResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    public TestResult run() {
        Log.e("run", "its called");
        testObjectEquals();
        return super.run();
    }

    public void testObjectEquals() {
        try {
            JSONObject jsonObject = new JSONObject("{\"status\": \"D\"}");
            if(jsonObject.getString("status").equals("D"))
                Log.e("status", "is D -- getString");
            if(jsonObject.get("status").equals("D"))
                Log.e("status", "is D -- get");
            String status = jsonObject.getString("status");
            if(status.equals("D"))
                Log.e("status", "is D -- status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}