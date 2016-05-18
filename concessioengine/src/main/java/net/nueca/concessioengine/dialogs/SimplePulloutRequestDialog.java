package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gama on 10/5/15.
 */
@Deprecated
public class SimplePulloutRequestDialog extends BasePulloutRequestDialog {
//    private TextView tvSourceBranchLabel, tvDestinationBranchLabel;
    private Button btnSave, btnCancel;
    private TextView tvTitle;

    private String dTitle = "Pullout Reason";

    private PulloutRequestDialogListener listener;

    public SimplePulloutRequestDialog(Context context, ImonggoDBHelper2 imonggoDBHelper) {
        super(context, imonggoDBHelper);
    }

    public SimplePulloutRequestDialog(Context context, ImonggoDBHelper2 dbHelper, int theme) {
        super(context, dbHelper, theme);
    }

    public SimplePulloutRequestDialog(Context context, List<DocumentPurpose> reasons, ImonggoDBHelper2 imonggoDBHelper) {
        super(context, reasons, imonggoDBHelper);
    }

    public SimplePulloutRequestDialog(Context context, List<DocumentPurpose> reasons, ImonggoDBHelper2 dbHelper, int theme) {
        super(context, reasons, dbHelper, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_pullout_reason_dialog2);
        super.setCancelable(false);

        llSourceBranch = (LinearLayout) super.findViewById(R.id.llSourceBranch);
        llDestinationBranch = (LinearLayout) super.findViewById(R.id.llDestinationBranch);
        tvTitle = (TextView) super.findViewById(R.id.tvTitle);
        spnReason = (Spinner) super.findViewById(R.id.spnReason);
        spnSourceBranch = (Spinner) super.findViewById(R.id.spnSourceBranch);
        spnDestinationBranch = (Spinner) super.findViewById(R.id.spnDestinationBranch);

        tvTitle.setText(dTitle);

        ArrayAdapter<DocumentPurpose> reasonAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, getReasonList());
        ArrayAdapter<String> sourceBranchAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, getSourceBranch());
        spnReason.setAdapter(reasonAdapter);

        int indexSelection = getReasonList().indexOf(currentReason);
        if(indexSelection > -1)
            spnReason.setSelection(indexSelection);
        spnSourceBranch.setAdapter(sourceBranchAdapter);

        try {
            ArrayAdapter<String> destinationBranchAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, getDestinationBranch());
            spnDestinationBranch.setAdapter(destinationBranchAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        spnReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showBranchSelection(((DocumentPurpose) spnReason.getSelectedItem())
                        .isSourceDestinationBranchDependent());
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    try {
                        listener.onSave(((DocumentPurpose)spnReason.getSelectedItem()),
                                getSelectedBranch(spnSourceBranch), getSelectedBranch(spnDestinationBranch));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onCancel();
                cancel();
            }
        });

        showBranchSelection(shouldShowBranchSelection);
    }

    public void setDTitle(String title) {
        this.dTitle = title;
    }

    public void setListener(PulloutRequestDialogListener listener) {
        this.listener = listener;
    }

    public interface PulloutRequestDialogListener {
        void onSave(DocumentPurpose reason, Branch source, Branch destination);
        void onCancel();
    }

    public Branch getSelectedSourceBranch() throws SQLException {
        return getSelectedBranch(spnSourceBranch);
    }
    public Branch getSelectedDestinationBranch() throws SQLException {
        return getSelectedBranch(spnDestinationBranch);
    }
    public DocumentPurpose getSelectedReason() {
        return (DocumentPurpose)spnReason.getSelectedItem();
    }
}
