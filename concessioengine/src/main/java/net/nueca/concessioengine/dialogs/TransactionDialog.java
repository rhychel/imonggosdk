package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.R;

/**
 * Created by rhymart on 12/3/15.
 */
public class TransactionDialog extends BaseAppCompatDialog {

    private Button btnDone;

    public TransactionDialog(Context context) {
        super(context);
    }

    public TransactionDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.transaction_complete_dialog);

        btnDone = (Button) super.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
