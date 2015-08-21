package net.nueca.concessio_test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public class C_Login3 extends ImonggoAppCompatActivity {

    private SlidingUpPanelLayout sliding_layout;
    private TextView tvTapMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);
        try {
            if (AccountTools.isLoggedIn(getHelper()))
                Log.e("Account", "I'm logged in!");
            else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        tvTapMe = (TextView) findViewById(R.id.tvTapMe);

        sliding_layout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i("SlidingUpPanelLayout", "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i("SlidingUpPanelLayout", "onPanelExpanded");

            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i("SlidingUpPanelLayout", "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i("SlidingUpPanelLayout", "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i("SlidingUpPanelLayout", "onPanelHidden");
            }
        });
        tvTapMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sliding_layout.setPanelState((sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                        ? SlidingUpPanelLayout.PanelState.COLLAPSED : SlidingUpPanelLayout.PanelState.ANCHORED);
            }
        });
        sliding_layout.setAnchorPoint(0.5f);
    }

    @Override
    public void onBackPressed() {
        Log.e("sliding_layout", sliding_layout.getPanelState().ordinal() + "");
        if (sliding_layout != null &&
                (sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

}
