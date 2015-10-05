package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Branch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gama on 10/5/15.
 */
public class SimplePulloutRequestDialog extends BasePulloutRequestDialog {
    public TextView tvSourceBranchLabel, tvDestinationBranchLabel;
    private PulloutRequestDialogListener listener;

    public SimplePulloutRequestDialog(Context context, ImonggoDBHelper imonggoDBHelper) {
        super(context, imonggoDBHelper);
        List<String> reasons = Arrays.asList(getContext().getResources()
                .getStringArray(R.array.string_array_pullout_reason));
        setReasonList(reasons);
    }

    public SimplePulloutRequestDialog(Context context, List<String> reasons, ImonggoDBHelper imonggoDBHelper) {
        super(context, reasons, imonggoDBHelper);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_pullout_reason_dialog);

        tvSourceBranchLabel = (TextView) super.findViewById(R.id.tvSourceBranchLabel);
        tvDestinationBranchLabel = (TextView) super.findViewById(R.id.tvDestinationBranchLabel);

        spnReason = (Spinner) super.findViewById(R.id.spnReason);
        spnSourceBranch = (Spinner) super.findViewById(R.id.spnSourceBranch);
        spnDestinationBranch = (Spinner) super.findViewById(R.id.spnDestinationBranch);

        spnReason.setAdapter( new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getReasonList()) );
        spnSourceBranch.setAdapter( new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getSourceBranch()) );
        try {
            spnDestinationBranch.setAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_spinner_item, getDestinationBranch()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        spnReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null)
                    listener.onReasonSelect((String) spnReason.getSelectedItem());

                showBranchSelection(((String) spnReason.getSelectedItem() ).toLowerCase().equals("transfer " +
                        "to branch"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnSourceBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((ArrayAdapter)spnDestinationBranch.getAdapter()).clear();
                    ((ArrayAdapter)spnDestinationBranch.getAdapter()).addAll(getDestinationBranch());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if(listener != null)
                    listener.onSourceSelect(getSelectedBranch(spnSourceBranch));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnDestinationBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(listener != null)
                    listener.onDestinationSelect(getSelectedBranch(spnDestinationBranch));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void showBranchSelection(boolean shouldShow) {
        super.showBranchSelection(shouldShow);
        tvSourceBranchLabel.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        tvDestinationBranchLabel.setVisibility(shouldShow? View.VISIBLE : View.GONE);
    }

    public interface PulloutRequestDialogListener {
        void onReasonSelect(String selectedReason);
        void onSourceSelect(Branch selectedBranch);
        void onDestinationSelect(Branch selectedBranch);
    }
}
