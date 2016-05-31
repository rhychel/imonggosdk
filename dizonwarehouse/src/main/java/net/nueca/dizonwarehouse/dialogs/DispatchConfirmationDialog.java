package net.nueca.dizonwarehouse.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.nueca.dizonwarehouse.R;
import net.nueca.imonggosdk.objects.Branch;

/**
 * Created by gama on 30/05/2016.
 */
public class DispatchConfirmationDialog extends AppCompatDialog {
    private TextView tvDateTime;
    private EditText etPlate, etCrates, etBranch;

    private Button btnCancel, btnSubmit;

    public interface DispatchConfirmationListener {
        void onCancel();
        boolean onSubmit(String plateNumber, String crates, Branch branch);
    }

    private DispatchConfirmationListener confirmationListener;
    private Branch targetBranch;

    public DispatchConfirmationDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.wh_dispatch_confirm_dialog);

        tvDateTime = (TextView) super.findViewById(R.id.tvDateTime);

        etPlate = (EditText) super.findViewById(R.id.etPlate);
        etCrates = (EditText) super.findViewById(R.id.etCrates);
        etBranch = (EditText) super.findViewById(R.id.etBranch);

        if(targetBranch != null)
            etBranch.setText(targetBranch.getName());

        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSubmit = (Button) super.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(confirmationListener != null)
                    confirmationListener.onCancel();
                cancel();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(confirmationListener != null) {
                    if (confirmationListener.onSubmit(etPlate.getText().toString(), etCrates.getText().toString(), targetBranch))
                        dismiss();
                }
                else
                    dismiss();
            }
        });
    }

    public DispatchConfirmationListener getConfirmationListener() {
        return confirmationListener;
    }

    public void setConfirmationListener(DispatchConfirmationListener confirmationListener) {
        this.confirmationListener = confirmationListener;
    }

    public Branch getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(Branch targetBranch) {
        this.targetBranch = targetBranch;
        if(etBranch != null)
            etBranch.setText(this.targetBranch.getName());
    }
}
